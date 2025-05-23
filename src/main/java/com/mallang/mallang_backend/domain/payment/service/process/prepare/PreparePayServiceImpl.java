package com.mallang.mallang_backend.domain.payment.service.process.prepare;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.PAYMENT_NOT_FOUND;
import static com.mallang.mallang_backend.global.exception.ErrorCode.PAYMENT_PROCESSING_PREPARED_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreparePayServiceImpl implements PreparePayService {

    private final ApplicationEventPublisher publisher;
    private final PaymentRedisService redisService;
    private final PaymentRepository paymentRepository;

    /**
     * 기본 결제 로직 트랜잭션 분리
     * <p>
     * 외부 API 호출 전 정보 저장 및 검증
     */
    @Override
    @Transactional
    @Retry(name = "dataSaveInstance", fallbackMethod = "preparePaymentFallback")
    public void preparePayment(PaymentApproveRequest request) {
        // 1. 멱등성 키 검증 및 저장 (트랜잭션 내)
        redisService.checkAndSaveIdempotencyKey(request.getIdempotencyKey());

        // 2. 결제 요청 값이 정확한지 검증 (Redis 체크)
        redisService.checkOrderIdAndAmount(request.getOrderId(), request.getAmount());

        // 3. 결제 상태 IN_PROGRESS로 업데이트 (트랜잭션 내)
        updatePaymentStatus(request.getOrderId(), PayStatus.IN_PROGRESS);
    }

    /**
     * 주문 ID에 해당하는 결제의 상태를 변경하고, 결제 상태 변경 이벤트를 발행합니다.
     *
     * @param orderId          주문 ID
     * @param changedPayStatus 변경할 결제 상태
     */
    public void updatePaymentStatus(String orderId,
                                    PayStatus changedPayStatus) {
        paymentRepository.findByOrderIdWithLock(orderId);
        Payment payment = paymentRepository.findByOrderIdWithLock(orderId).orElseThrow(
                () -> new ServiceException(PAYMENT_NOT_FOUND));
        payment.updateStatus(changedPayStatus); // 단순 상태 업데이트, Redis에서 동시성을 제한한 검증 중 -> 동시성 문제가 생기기 어렵긴 할 듯...

        // 로그: 상태 변경 이벤트 발행
        publisher.publishEvent(new PaymentUpdatedEvent(
                payment.getId(),
                changedPayStatus,
                changedPayStatus.getDescription()
        ));
    }

    public void preparePaymentFallback(PaymentApproveRequest request,
                                       Throwable t) {
        log.error("[결제 정보 저장 실패] orderId: {}", request.getOrderId());
        throw new ServiceException(PAYMENT_PROCESSING_PREPARED_FAILED, t.getCause());
    }

}
