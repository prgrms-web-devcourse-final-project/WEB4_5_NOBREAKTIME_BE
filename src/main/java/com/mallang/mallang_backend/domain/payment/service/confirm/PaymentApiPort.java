package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;

public interface PaymentApiPort {

    PaymentResponse callTossPaymentAPI(PaymentApproveRequest request);
}
