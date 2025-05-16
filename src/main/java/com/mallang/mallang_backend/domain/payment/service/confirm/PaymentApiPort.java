package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.request.BillingPaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;

public interface PaymentApiPort {

    PaymentResponse callTossPaymentAPI(PaymentApproveRequest request);

    // 빌링 키 추출
    String callTossPaymentBillingAPI(BillingPaymentRequest request);

    // 빌링 키를 이용해서 결제 요청을 전송 -> 결과 반환
    BillingPaymentResponse payWithBillingKey(String billingKey,
                                             BillingPaymentRequest request
    );
}
