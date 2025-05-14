package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentSimpleRequest;

public interface PaymentRequestService {

    PaymentRequest createPaymentRequest(Long memberId,
                                        PaymentSimpleRequest simpleRequest);
}
