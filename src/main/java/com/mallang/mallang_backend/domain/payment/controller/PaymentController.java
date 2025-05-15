package com.mallang.mallang_backend.domain.payment.controller;

import com.mallang.mallang_backend.domain.payment.docs.PaymentRequestDocs;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRequestService;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentRequestService requestService;


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

    // TODO 성공 경우
    /**
     * /success?paymentType={PAYMENT_TYPE}&orderId={ORDER_ID}
     * &paymentKey={PAYMENT_KEY}&amount={AMOUNT}
     */

    // TODO 실패 경우
    /**
     * /fail?code={ERROR_CODE}&message={ERROR_MESSAGE}
     * &orderId={ORDER_ID}
     */
}


