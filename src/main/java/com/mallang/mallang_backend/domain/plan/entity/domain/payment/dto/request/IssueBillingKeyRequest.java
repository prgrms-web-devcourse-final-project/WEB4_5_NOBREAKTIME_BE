package com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssueBillingKeyRequest {

    private String customerKey;
    private String authKey;
}