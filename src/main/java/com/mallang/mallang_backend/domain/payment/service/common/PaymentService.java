package com.mallang.mallang_backend.domain.payment.service.common;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.confirm.PaymentConfirmService;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.PAYMENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;
    private final PaymentRedisService redisService;
    private final PaymentConfirmService paymentConfirmService;

    public MemberGrantedInfo getMemberId(String orderId) {
        Payment payment = findByOrderIdThrows(orderId);
        return new MemberGrantedInfo(
                payment.getMemberId(),
                payment.getPlan().getType().getRoleName());
    }

    public void checkIdemkeyAndSave(String idempotencyKey
    ) {
       redisService.checkIdemkeyAndSave(idempotencyKey);
    }

    public record MemberGrantedInfo(Long memberId, String roleName) {
    }

    @Transactional
    public void updatePaymentStatus(String orderId,
                                    PayStatus changedPayStatus
    ) {
        Payment payment = findByOrderIdThrows(orderId);
        payment.updatePayStatus(changedPayStatus);

        // 결제 히스토리 업데이트
        publisher.publishEvent(new PaymentUpdatedEvent(
                payment.getId(),
                changedPayStatus,
                changedPayStatus.getDescription()
        ));
    }

    // 결제 승인 요청 전송 후 응답 객체 반환
    public PaymentResponse sendApproveRequest(PaymentApproveRequest request
    ) {
        updatePaymentStatus(request.getOrderId(), PayStatus.IN_PROGRESS); // 결제 승인 대기 상태로 업데이트
        return paymentConfirmService.sendApproveRequest(request);
    }

    // DB에 저장
    @Transactional
    public void processPaymentResult(String orderId,
                                     PaymentResponse result
    ) {
        paymentConfirmService.processPaymentResult(orderId, result);
    }

    private Payment findByOrderIdThrows(String orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() ->
                new ServiceException(PAYMENT_NOT_FOUND));
    }
}
