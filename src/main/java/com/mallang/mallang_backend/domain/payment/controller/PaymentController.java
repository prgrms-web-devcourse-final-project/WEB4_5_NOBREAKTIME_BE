package com.mallang.mallang_backend.domain.payment.controller;

import com.mallang.mallang_backend.domain.payment.docs.PaymentRequestDocs;
import com.mallang.mallang_backend.domain.payment.dto.after.PaymentFailureRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.service.common.PaymentService;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRequestService;
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
public class PaymentController {

    private final PaymentRequestService requestService;
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

        paymentService.checkIdemkeyAndSave(request.getIdempotencyKey()); // 중복 호출 확인
        paymentService.updatePaymentStatus(
                request.getOrderId(),
                PayStatus.IN_PROGRESS
        ); // 결제 승인 대기 상태로 업데이트

        PaymentResponse result = paymentService.sendApproveRequest(request); // 결제 API 호출 (승인 로직 진행)

        paymentService.processPaymentResult(
                request.getOrderId(),
                result
        ); // DB 저장 및 성공 및 실패 이벤트 발행

        MemberGrantedInfo grantedInfo = paymentService
                .getMemberId(request.getOrderId()); // 새로운 권한 정보 발행

        Long memberId = grantedInfo.memberId();
        String roleName = grantedInfo.roleName();

        setSecurityContext(memberId, roleName); // 시큐리티 객체 업데이트
        setNewJwtTokens(response, memberId, roleName); // 토큰 정보 업데이트

        RsData<String> rsp = new RsData<>(
                "200",
                "결제 승인 완료");

        return ResponseEntity.ok(rsp);
    }

    @Operation(
            summary = "결제 승인 실패 처리",
            description = "결제 요청에 실패한 경우 결제 상태를 ABORTED로 변경하고 실패 로그를 기록합니다."
    )
    @PostMapping("/fail") // 요청 자체가 실패한 경우
    public ResponseEntity<RsData<String>> failedPayment(
            @Valid @RequestBody PaymentFailureRequest request
    ) {
        paymentService.updatePaymentStatus(
                request.getOrderId(),
                PayStatus.ABORTED
        ); // 결제 상태 업데이트 및 실패 로그 기록

        RsData<String> rsp = new RsData<>(
                "200",
                "결제 승인 실패 업데이트 완료"
        );

        return ResponseEntity.status(200).body(rsp);
    }

    // 구독한 권한에 맞는 시큐리티 인증 정보를 재발급합니다.
    private void setSecurityContext(Long memberId,
                                    String roleName
    ) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        memberId,
                        null,
                        List.of(new SimpleGrantedAuthority(roleName))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 구독한 권한에 맞는 jwt 토큰을 재발급합니다.
    private void setNewJwtTokens(HttpServletResponse response,
                                 Long memberId,
                                 String roleName
    ) {
        TokenPair tokenPair = tokenService.createTokenPair(
                memberId,
                roleName
        );
        jwtService.setJwtSessionCookie(
                tokenPair.getAccessToken(),
                response
        );
        jwtService.setJwtPersistentCookie(
                tokenPair.getRefreshToken(),
                response
        );
    }
}


