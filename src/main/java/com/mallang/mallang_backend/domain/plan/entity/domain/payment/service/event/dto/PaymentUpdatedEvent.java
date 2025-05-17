package com.mallang.mallang_backend.domain.plan.entity.domain.payment.service.event.dto;

import com.mallang.mallang_backend.domain.plan.entity.domain.payment.entity.PayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentUpdatedEvent {

    private Long paymentId;
    private PayStatus status;
    private String message;
}
