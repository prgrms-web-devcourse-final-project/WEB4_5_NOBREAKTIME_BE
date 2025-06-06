package com.mallang.mallang_backend.domain.payment.event.dto;

import lombok.*;

/*
 * 결제 실패 이벤트
 */
@Data
@Builder
@AllArgsConstructor
public class PaymentFailedEvent {

    private Long paymentId;
    private String code;
    private String message;
}
