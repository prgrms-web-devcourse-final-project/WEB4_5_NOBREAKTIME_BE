package com.mallang.mallang_backend.domain.payment.dto;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Schema(description = "결제 요청 정보 반환을 위해 클라이언트 측에서 전송하는 DTO 객체")
public class PaymentSimpleRequest {

    @Schema(description = "구독 정보", example = "STANDARD")
    @NotNull(message = "구독을 선택해 주세요.")
    private SubscriptionType type;

    @Schema(description = "플랜 정보", example = "MONTHLY")
    @NotNull(message = "구독 일수를 선택해 주세요.")
    private PlanPeriod period;
}
