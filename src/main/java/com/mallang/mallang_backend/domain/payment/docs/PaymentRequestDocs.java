package com.mallang.mallang_backend.domain.payment.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "결제 요청 응답 Wrapper (문서화용)")
public class PaymentRequestDocs extends RsDataDocs {

    @Schema(description = "결제 응답 데이터")
    private Data data;

    public PaymentRequestDocs(String code, String msg, Data data) {
        super(code, msg);
        this.data = data;
    }

    // 문서화용 데이터 클래스
    @Getter
    @AllArgsConstructor
    @Schema(description = "결제 응답 데이터 (문서화용)")
    public static class Data {
        @Schema(description = "주문 고유번호", example = "20240510-랜덤값5글자-1")
        private String orderId;

        @Schema(description = "주문명", example = "STANDARD 정기 구독")
        private String orderName;

        @Schema(description = "결제 금액", example = "15000")
        private int amount;

        @Schema(description = "성공 URL", example = "https://api.mallang.site/success")
        private String successUrl;

        @Schema(description = "실패 URL", example = "https://api.mallang.site/fail")
        private String failUrl;
    }
}
