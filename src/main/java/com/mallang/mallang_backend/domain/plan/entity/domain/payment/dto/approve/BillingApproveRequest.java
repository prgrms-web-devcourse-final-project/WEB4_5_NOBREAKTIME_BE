package com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.approve;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BillingApproveRequest {

    private String customerKey;
    private int amount;
    private String orderId;
    private String orderName;
}
