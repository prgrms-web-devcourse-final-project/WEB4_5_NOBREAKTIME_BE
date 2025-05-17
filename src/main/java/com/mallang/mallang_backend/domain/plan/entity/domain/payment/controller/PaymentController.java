package com.mallang.mallang_backend.domain.plan.entity.domain.payment.controller;

import com.mallang.mallang_backend.domain.plan.entity.domain.payment.docs.PaymentRequestDocs;
import com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.after.PaymentFailureRequest;
import com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.request.BillingPaymentRequest;
import com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.request.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.plan.entity.domain.payment.service.common.PaymentService;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenPair;
import com.mallang.mallang_backend.global.token.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mallang.mallang_backend.domain.payment.service.common.PaymentService.MemberGrantedInfo;
import static com.mallang.mallang_backend.global.exception.ErrorCode.API_ERROR;
import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
@Tag(name = "결제 관리 API", description = "빌링 키 발급 및 자동 결제 관리를 위한 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtService jwtService;
    private final TokenService tokenService;

    @Operation(
            summary = "결제 요청 정보 생성",
            description = "로그인한 회원의 정보와 결제 요청 데이터를 받아 결제 요청 정보를 생성합니다."
    )
    @ApiResponse(content = @Content(schema = @Schema(implementation = PaymentRequestDocs.class)))
    @PossibleErrors({PLAN_NOT_FOUND, PAYMENT_NOT_FOUND, MEMBER_NOT_FOUND})
    @PostMapping("/request")
    @TimeTrace
    public ResponseEntity<RsData<PaymentRequest>> createPaymentRequest(
            @Parameter(hidden = true) @Login CustomUserDetails userDetails,
            @Valid @RequestBody PaymentSimpleRequest simpleRequest) {

        RsData<PaymentRequest> response = new RsData<>(
                "200",
                "결제 요청 정보를 생성 및 전송 성공",
                paymentService.createPaymentRequest(
                        userDetails.getMemberId(),
                        simpleRequest));

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 요청 성공 이후 결제 승인 로직",
            description = "결제 승인 요청을 전송하고, 결제 상태를 업데이트하며, 권한 정보 및 JWT 토큰을 갱신합니다."
    )
    @TimeTrace
    @PossibleErrors({PAYMENT_NOT_FOUND, PAYMENT_CONFIRM_FAIL, PAYMENT_CONFLICT})
    @PostMapping("/confirm")
    public ResponseEntity<RsData<String>> succeedPayment(
            @Valid @RequestBody PaymentApproveRequest request,
            HttpServletResponse response
    ) {

        MemberGrantedInfo grantedInfo = paymentService.processPaymentAndUpdateSubscription(request);

        Long memberId = grantedInfo.memberId();
        String roleName = grantedInfo.roleName();

        setSecurityContext(memberId, roleName);
        setNewJwtTokens(response, memberId, roleName);

        RsData<String> rsp = new RsData<>(
                "200",
                "결제 승인 완료");

        return ResponseEntity.ok(rsp);
    }

    @Operation(
            summary = "결제 요청 실패 처리",
            description = "결제 요청에 실패한 경우 결제 상태를 ABORTED로 변경하고 실패 로그를 기록합니다."
    )
    @PostMapping("/fail")
    public ResponseEntity<RsData<String>> failedPayment(
            @Valid @RequestBody PaymentFailureRequest request
    ) {

        paymentService.handleFailedPayment(request.getOrderId(), request.getCode());

        RsData<String> rsp = new RsData<>(
                "200",
                "결제 승인 실패 업데이트 완료"
        );

        return ResponseEntity.status(200).body(rsp);
    }

    // ======== 자동 결제 ======== //

    /**
     * 빌링 키 발급 ( + 자동 결제 요청을 처리)
     *
     * 처리 프로세스:
     * 1. 결제 서비스로부터 빌링 키 발급
     * 2. 고객 정보에 빌링 키 및 고객 키 업데이트
     *
     * [이후 자동 결제 시]
     * 1. 빌링 키 / 고객 키를 이용해서 자동 결제 승인 요청
     * 2. 결제 결과 처리 및 최종 응답 반환
     *
     * @param request 결제 요청 정보 (주문 ID, 고객 키 등)
     * @return 처리 결과 메시지 (성공 시 200 코드 반환)
     */
    @Operation(
            summary = "자동 결제 키(빌링 키) 발급",
            description = "빌링 키를 발급하고 제대로 발급이 되었을 때에는 200, " +
                    "발급에 문제가 생겼을 경우 error 가 발생합니다." +
                    "제대로 처리되지 않았을 경우에는 단순히 한 달 결제가 된 것으로 판단합니다." +
                    "이후에 갱신이 가능하지 않도록 처리하고, 내역에서도 갱신 취소가 불가능합니다."
    )
    @PossibleErrors({PAYMENT_CONFIRM_FAIL, API_ERROR, PAYMENT_NOT_FOUND})
    @PostMapping("/issue-billing-key")
    public ResponseEntity<RsData<String>> succeedBillingPayment(
            @Valid @RequestBody BillingPaymentRequest request) {

       paymentService.processIssueBillingKey(request);

        RsData<String> response = new RsData<>(
                "200",
                "자동 빌링 키 발급 완료"
        );

        return ResponseEntity.ok(response);
    }

    // 테스트 코드
    public ResponseEntity<String> autoBillingPayment(
            @Login CustomUserDetails userDetails) {
        // autoBillingService.executeAutoBilling();

        return ResponseEntity.ok("자동 결제 완료");
    }

    // ======== 구독 해지 ======== //
    @PatchMapping("/cancel")
    public ResponseEntity<RsData<String>> cancelSubscription(
            @Parameter(hidden = true) @Login CustomUserDetails userDetails
    ) {
        paymentService.cancelSubscription(userDetails.getMemberId());

        RsData<String> response = new RsData<>(
                "200",
                "구독 해지 완료"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Security Context에 사용자 인증 정보를 설정합니다.
     *
     * @param memberId 설정할 회원 식별자
     * @param roleName 부여할 권한 이름 (예: ROLE_USER, ROLE_ADMIN)
     */
    private void setSecurityContext(Long memberId,
                                    String roleName) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(memberId, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * JWT 토큰 쌍을 생성하고 HTTP 응답에 쿠키로 설정합니다.
     * <p>
     * 액세스 토큰은 세션 쿠키로, 리프레시 토큰은 영속 쿠키로 설정됩니다.
     *
     * @param response HTTP 응답 객체
     * @param memberId 토큰 발급 대상 회원 식별자
     * @param roleName 권한 정보 (토큰 클레임에 포함)
     */
    private void setNewJwtTokens(HttpServletResponse response,
                                 Long memberId,
                                 String roleName) {

        // 1. 토큰 페어 생성
        TokenPair tokenPair = tokenService.createTokenPair(memberId, roleName);

        // 2. 쿠키 설정
        jwtService.setJwtSessionCookie(tokenPair.getAccessToken(), response);
        jwtService.setJwtPersistentCookie(tokenPair.getRefreshToken(), response);
    }
}


