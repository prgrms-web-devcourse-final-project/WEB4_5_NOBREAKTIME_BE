package com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueBillingKeyResponse {

    private String billingKey;
    private String authenticatedAt;
}
