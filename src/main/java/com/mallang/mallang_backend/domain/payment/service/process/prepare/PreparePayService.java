package com.mallang.mallang_backend.domain.payment.service.process.prepare;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;

public interface PreparePayService {
    void preparePayment(PaymentApproveRequest request);
}
