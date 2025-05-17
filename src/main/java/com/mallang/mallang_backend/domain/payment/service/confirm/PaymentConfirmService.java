package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;

public interface PaymentConfirmService {
    // 결제 승인 요청 전송 후 응답 반환
    PaymentResponse sendApproveRequest(PaymentApproveRequest request);

    // 결제 응답을 DB에 저장
    void processPaymentResult(String orderId,
                              PaymentResponse result);

    //  자동 결제 프로세스
    void processAutoBillingPaymentResult(BillingPaymentResponse response);
}

