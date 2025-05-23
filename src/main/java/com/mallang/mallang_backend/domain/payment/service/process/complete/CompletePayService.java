package com.mallang.mallang_backend.domain.payment.service.process.complete;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;

import static com.mallang.mallang_backend.domain.payment.service.process.complete.CompletePayServiceImpl.MemberGrantedInfo;

public interface CompletePayService {
    // 기본 결제 로직 트랜잭션 분리
    MemberGrantedInfo completePayment(PaymentResponse response);
}
