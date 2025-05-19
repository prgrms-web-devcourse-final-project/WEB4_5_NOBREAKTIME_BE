package com.mallang.mallang_backend.domain.payment.scheduler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionRenewalDto {

    private Long memberId;
    private String planDescription;
}