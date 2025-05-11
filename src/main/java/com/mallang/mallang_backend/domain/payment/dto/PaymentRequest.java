package com.mallang.mallang_backend.domain.payment.dto;

import com.mallang.mallang_backend.domain.payment.entity.Payment;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PaymentRequest {

    private String orderId;
    private String orderName;
    private int amount;
    private String successUrl;
    private String failUrl;

    public static PaymentRequest from(
            Payment payment,
            String successUrl,
            String failUrl
    ) {
        PaymentRequest request = new PaymentRequest();

        request.setOrderId(payment.getOrderId());
        request.setOrderName(payment.getPlan().getDescription());
        request.setAmount(payment.getTotalAmount());
        request.setSuccessUrl(successUrl);
        request.setFailUrl(failUrl);

        return request;
    }
}



