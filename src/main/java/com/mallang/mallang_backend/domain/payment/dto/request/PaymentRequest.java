package com.mallang.mallang_backend.domain.payment.dto.request;

import com.mallang.mallang_backend.domain.payment.entity.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PaymentRequest {

    private String orderId;
    private String orderName;
    private int amount;
    private String currency = "KRW";

    public static PaymentRequest from(
            Payment payment
    ) {
        PaymentRequest request = new PaymentRequest();

        request.setOrderId(payment.getOrderId());
        request.setOrderName(payment.getPlan().getDescription());
        request.setAmount(payment.getTotalAmount());

        return request;
    }
}



