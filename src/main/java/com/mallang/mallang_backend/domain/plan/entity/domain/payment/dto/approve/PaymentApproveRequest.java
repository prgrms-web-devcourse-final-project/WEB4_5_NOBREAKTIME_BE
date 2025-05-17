package com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 결제 승인 요청 정보를 담는 DTO입니다.
 */
@Getter
@Builder
@ToString
@Schema(description = "결제 승인 요청 DTO")
public class PaymentApproveRequest {

    @Schema(
            description = "멱등성 키 (Idempotency-Key). 중복 결제 승인 요청을 방지하기 위한 고유 값입니다.",
            example = "랜덤값으로 30자 이내"
    )
    @NotBlank
    private String idempotencyKey;

    @Schema(
            description = "결제 키. 토스페이먼츠 결제창에서 결제 성공 시 전달받은 고유 키입니다.",
            example = "tgrn_abcdef1234567890"
    )
    @NotBlank
    private String paymentKey;

    @Schema(
            description = "결제 금액(원).",
            example = "10000"
    )
    private int amount;

    @Schema(
            description = "주문 고유 ID. 결제 요청 시 생성한 주문의 식별자입니다.",
            example = "20240510-랜덤값5글자-00001"
    )
    @NotBlank
    private String orderId;
}
