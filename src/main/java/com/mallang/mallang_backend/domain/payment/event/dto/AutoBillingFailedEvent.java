package com.mallang.mallang_backend.domain.payment.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AutoBillingFailedEvent {

    private Long paymentId;
}
