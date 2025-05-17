package com.mallang.mallang_backend.domain.plan.entity.domain.payment.dto.approve;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingPaymentResponse {

    private String orderId;
    private String orderName;
    private String status;
    private String approvedAt;
    private String paymentKey;
    private String method;
    private int totalAmount;
    private Receipt receipt;
    private Card cardInfo;
    private Failure failure;

    @Getter
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Card {

        private String acquireStatus;
        private String approveNo;
    }
}
