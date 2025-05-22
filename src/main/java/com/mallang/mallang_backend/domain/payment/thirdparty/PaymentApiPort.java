package com.mallang.mallang_backend.domain.payment.thirdparty;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;

public interface PaymentApiPort {
    // 일반 결제 요청
    PaymentResponse callTossPaymentAPI(PaymentApproveRequest request);

    // 빌링 키 추출
    String issueBillingKey(String customerKey, String authKey, String orderId);

    // 빌링 키를 이용해서 결제 요청을 전송 -> 결과 반환
    BillingPaymentResponse payWithBillingKey(String billingKey,
                                             String customerKey,
                                             String orderId,
                                             String orderName,
                                             int amount
    );
}
