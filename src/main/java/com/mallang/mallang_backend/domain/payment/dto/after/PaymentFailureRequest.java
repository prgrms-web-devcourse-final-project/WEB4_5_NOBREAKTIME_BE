package com.mallang.mallang_backend.domain.payment.dto.after;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 실패 정보 DTO")
public class PaymentFailureRequest {

    @Schema(description = "실패 코드", example = "PAYMENT_FAILED")
    String code;

    @Schema(description = "실패 메시지", example = "결제에 실패했습니다.")
    String message;

    @Schema(description = "주문 ID", example = "250525-asQja-00001")
    String orderId;
}
