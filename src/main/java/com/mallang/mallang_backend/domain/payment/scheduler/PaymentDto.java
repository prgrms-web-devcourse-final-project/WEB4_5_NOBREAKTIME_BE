package com.mallang.mallang_backend.domain.payment.scheduler;

import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentDto {
    Long id;
    String billingKey;
    String paymentKey;
    String customerKey;
    String orderId;
    PayStatus payStatus;
    int totalAmount;
    Long memberId;
    Plan plan;

    public static PaymentDto from(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .billingKey(payment.getBillingKey())
                .paymentKey(payment.getPaymentKey())
                .customerKey(payment.getCustomerKey())
                .orderId(payment.getOrderId())
                .payStatus(payment.getPayStatus())
                .totalAmount(payment.getTotalAmount())
                .memberId(payment.getMemberId())
                .plan(payment.getPlan())
                .build();
    }

}