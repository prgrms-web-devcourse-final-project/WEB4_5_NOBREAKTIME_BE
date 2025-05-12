package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.domain.payment.dto.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.PaymentSimpleRequest;

public interface PaymentRequestService {

    PaymentRequest createPaymentRequest(String idempotencyKey,
                                               Long memberId,
                                               PaymentSimpleRequest simpleRequest);
}
