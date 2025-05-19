package com.mallang.mallang_backend.domain.payment.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentMailSendEvent {

    private Long paymentId;
    private Long memberId;
    private String receiptUrl;
}
