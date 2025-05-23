package com.mallang.mallang_backend.domain.payment.service.process.error;

import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.PAYMENT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleErrorService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void handleApiFallback(String orderId) {
        // 결제 API 승인 실패 -> 로그 업데이트 필요
        Payment payment = paymentRepository.findByOrderIdWithLock(orderId).orElseThrow(
                () -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND)); // LOCK

        payment.updateStatus(PayStatus.FAILED);

        updatePaymentStatus(orderId, PayStatus.FAILED); // 별도 트랜잭션
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSaveFailedFallback(String orderId) {
        Payment payment = paymentRepository.findByOrderIdWithLock(orderId).orElseThrow(
                () -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND)); // LOCK

        log.error("[결제 결과 저장 실패 - 결제 취소 트리거] memberId: {}, orderId: {}",
                payment.getMemberId(),
                payment.getOrderId()
        );

        // 결제 취소 API 호출
        // TODO: POST /v1/payments/{paymentKey}/cancel 구현 필요

        log.info("[결제 취소 완료] memberId: {}, orderId: {}, 구독 플랜: {}, 금액: {}",
                payment.getMemberId(),
                payment.getOrderId(),
                payment.getPlan().getDescription(),
                payment.getTotalAmount()
        );

        payment.updateStatus(PayStatus.FAILED);
        updatePaymentStatus(orderId, PayStatus.FAILED);
    }

    /**
     * 주문 ID에 해당하는 결제의 상태를 변경하고, 결제 상태 변경 이벤트를 발행합니다.
     *
     * @param orderId          주문 ID
     * @param changedPayStatus 변경할 결제 상태
     */
    public void updatePaymentStatus(String orderId,
                                    PayStatus changedPayStatus) {
        Payment payment = findByOrderIdThrows(orderId);
        payment.updateStatus(changedPayStatus);

        // 결제 상태 변경 이벤트 발행
        publisher.publishEvent(new PaymentUpdatedEvent(
                payment.getId(),
                changedPayStatus,
                changedPayStatus.getDescription()
        ));
    }

    /**
     * 주문 ID로 결제 정보를 조회합니다. 존재하지 않으면 ServiceException을 발생시킵니다.
     *
     * @param orderId 주문 ID
     * @return 결제 정보
     * @throws ServiceException 결제 정보가 없을 경우
     */
    public Payment findByOrderIdThrows(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));
    }

}
