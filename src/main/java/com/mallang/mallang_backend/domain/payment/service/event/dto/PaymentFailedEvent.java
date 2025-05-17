package com.mallang.mallang_backend.domain.payment.service.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
