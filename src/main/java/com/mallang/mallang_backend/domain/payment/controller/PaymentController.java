package com.mallang.mallang_backend.domain.payment.controller;

import com.mallang.mallang_backend.domain.payment.docs.PaymentRequestDocs;
import com.mallang.mallang_backend.domain.payment.dto.after.PaymentFailureRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.request.BillingPaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.service.common.PaymentService;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRequestService;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
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

import static com.mallang.mallang_backend.domain.payment.service.common.PaymentService.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
@Tag(name = "결제 관리 API", description = "빌링 키 발급 및 자동 결제 관리를 위한 API")
public class PaymentController {

    private final PaymentRequestService requestService;
    private final PaymentService paymentService;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final SubscriptionService subscriptionService;

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

        PaymentRequest request = requestService.createPaymentRequest(
                userDetails.getMemberId(),
                simpleRequest);

        RsData<PaymentRequest> response = new RsData<>("200",
                "결제 요청 정보를 생성 및 전송 성공",
                request);
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

        paymentService.checkIdemkeyAndSave(request.getIdempotencyKey());
        paymentService.updatePaymentStatus(
                request.getOrderId(),
                PayStatus.IN_PROGRESS
        );

        PaymentResponse result = paymentService.sendApproveRequest(request);

        paymentService.processPaymentResult(
                request.getOrderId(),
                result
        );

        MemberGrantedInfo grantedInfo = paymentService
                .getMemberId(request.getOrderId());

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
            summary = "결제 승인 실패 처리",
            description = "결제 요청에 실패한 경우 결제 상태를 ABORTED로 변경하고 실패 로그를 기록합니다."
    )
    @PostMapping("/fail")
    public ResponseEntity<RsData<String>> failedPayment(
            @Valid @RequestBody PaymentFailureRequest request
    ) {
        paymentService.updatePaymentStatus(
                request.getOrderId(),
                PayStatus.ABORTED
        );

        RsData<String> rsp = new RsData<>(
                "200",
                "결제 승인 실패 업데이트 완료"
        );

        return ResponseEntity.status(200).body(rsp);
    }

    // ======== 자동 결제 ======== //

    /**
     * 빌링 키 발급 및 자동 결제 요청을 처리합니다.
     * <p>
     * 처리 프로세스:
     * 1. 결제 서비스로부터 빌링 키 발급
     * 2. 고객 정보에 빌링 키 및 고객 키 업데이트
     * 3. 결제 상태를 '진행 중'으로 변경
     * 4. 자동 결제 승인 요청 전송
     * 5. 결제 결과 처리 및 최종 응답 반환
     *
     * @param request 결제 요청 정보 (주문 ID, 고객 키 등)
     * @return 처리 결과 메시지 (성공 시 200 코드 반환)
     */
    @Operation(
            summary = "빌링 키 발급 및 결제 처리",
            description = "빌링 키 발급부터 자동 결제 승인까지의 전체 프로세스를 처리하는 API"
    )
    @PossibleErrors({PAYMENT_CONFIRM_FAIL, API_ERROR, PAYMENT_NOT_FOUND})
    @PostMapping("/confirm/issue-billing-key")
    public ResponseEntity<RsData<String>> succeedBillingPayment(
            @Valid @RequestBody BillingPaymentRequest request) {

        // 1. 빌링 키 발급
        String billingKey = paymentService.issueBillingKey(request);

        // 2. 고객 정보 업데이트
        paymentService.updateBillingKeyAndCustomerKey(
                request.getOrderId(),
                billingKey,
                request.getCustomerKey()
        );

        // 3. 결제 상태 변경
        paymentService.updatePaymentStatus(
                request.getOrderId(),
                PayStatus.IN_PROGRESS
        );

        // 4. 자동 결제 승인 요청
        BillingPaymentResponse approveResponse =
                paymentService.sendApproveAutoBillingRequest(
                        request,
                        billingKey
                );

        // 5. 결제 결과 처리
        paymentService.processAutoBillingPaymentResult(approveResponse);

        RsData<String> response = new RsData<>(
                "200",
                "구독 결제 완료"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 빌링 키 발급 실패 시 결제 상태를 업데이트합니다.
     *
     * 처리 프로세스:
     * 1. 회원 ID 기반 결제 내역 조회
     * 2. 결제 상태 ABORTED로 변경
     *
     * @param userDetails 인증된 사용자 정보
     * @return 처리 결과 메시지 (성공 시 200 코드 반환)
     */
    @Operation(
            summary = "빌링 키 발급 실패 처리",
            description = "카드 인증 실패 시 결제 상태를 중단 상태로 변경하는 API"
    )
    @PostMapping("/fail/issue-billing-key")
    public ResponseEntity<RsData<String>> failedBillingPayment(
            @Parameter(hidden = true) @Login CustomUserDetails userDetails) {

        // 1. 결제 내역 조회
        String orderId = paymentService.getPaymentByMemberId(userDetails.getMemberId());

        // 2. 결제 상태 업데이트
        paymentService.updatePaymentStatus(orderId, PayStatus.ABORTED);

        RsData<String> response = new RsData<>(
                "200",
                "결제 상태 업데이트 완료");

        return ResponseEntity.ok(response);
    }

    // ======== 구독 해지 ======== //
    @PatchMapping("/cancel")
    public ResponseEntity<RsData<String>> cancelSubscription(
            @Parameter(hidden = true) @Login CustomUserDetails userDetails
    ) {
        subscriptionService.cancelSubscription(userDetails.getMemberId());

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


