package com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@Schema(description = "카드 등록 승인 후 자동 결제 키(빌링 키)를 발급, 자동 결제 로직을 실행하기 위한 DTO")
public class BillingPaymentRequest {

    @NotBlank
    @Schema(description = "고객 식별 키 (빌링 키 발급용)", example = "랜덤한 30글자 이내의 값, 멱등성 토큰과 동일하게 제작")
    private String customerKey;

    @NotBlank
    @Schema(description = "카드 인증 성공 시 발급된 승인 키", example = "auth_9876543210")
    private String authKey;

    @NotBlank
    @Schema(description = "결제 주문 ID. 빌링 키 발급 후 결제 요청에 사용", example = "250515-Ajks9-00001")
    private String orderId;

    @NotBlank
    @Schema(description = "주문명. 빌링 키 발급 후 결제 요청에 사용", example = "프리미엄 정기 구독")
    private String orderName;

    @Schema(description = "결제 금액. 빌링 키 발급 후 결제 요청에 사용", example = "10000")
    private int amount;
}
