package com.mallang.mallang_backend.domain.payment.service.process;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.service.process.complete.CompletePayService;
import com.mallang.mallang_backend.domain.payment.service.process.complete.CompletePayServiceImpl.MemberGrantedInfo;
import com.mallang.mallang_backend.domain.payment.service.process.prepare.PreparePayService;
import com.mallang.mallang_backend.domain.payment.thirdparty.PaymentApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 결제 파사드 서비스 (트랜잭션 분리 조정)
 */
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final PreparePayService preparePayService; // 결제 전 처리 (DB 저장)
    private final PaymentApiPort apiPort; // 외부 결제 API 호출 (트랜잭션 외부)
    private final CompletePayService completePayService; // 결제 후 처리 (결제 결과 처리 및 관련 이벤트 발생)

    public MemberGrantedInfo executePayment(PaymentApproveRequest request) {
        preparePayService.preparePayment(request);
        PaymentResponse response = apiPort.callTossPaymentAPI(request);// 외부 결제 API 호출 (트랜잭션 외부)
        return completePayService.completePayment(response);
    }
}
