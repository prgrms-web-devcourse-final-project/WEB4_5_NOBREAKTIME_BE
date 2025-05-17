package com.mallang.mallang_backend.domain.payment.dto.approve;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String approvedAt;
    private Object card; // 결제 시에만 값이 들어옴 (nullable)
    private Object virtualAccount;
    private Object transfer;
    private Object mobilePhone;
    private Object giftCertificate;
    private Object cashReceipt;
    private Object discount;
    private Object cancels;
    private String secret;
    private String type;
    private EasyPay easyPay;
    private Failure failure;
    private int totalAmount;
    private String method;
    private Receipt receipt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EasyPay {
        private String provider;
    }
}
