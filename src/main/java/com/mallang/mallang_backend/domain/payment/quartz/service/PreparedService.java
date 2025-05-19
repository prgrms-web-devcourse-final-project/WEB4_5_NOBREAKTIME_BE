package com.mallang.mallang_backend.domain.payment.quartz.service;

import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.event.dto.AutoBillingFailedEvent;
import com.mallang.mallang_backend.domain.payment.quartz.dto.PaymentDto;
import com.mallang.mallang_backend.domain.payment.repository.PaymentQueryRepository;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.common.PaymentService;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRequestService;
import com.mallang.mallang_backend.domain.payment.thirdparty.PaymentApiPortImpl;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreparedService {

    private final PaymentQueryRepository paymentQueryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final ApplicationEventPublisher publisher;
    private final PaymentRequestService paymentRequestService;

    public PaymentDto getPreviousPayment(Long memberId) {
        Payment previousPayment = getLastPayment(memberId);
        log.debug("최근 결제 정보 조회 ID: {}", previousPayment.getId());

        BillingKeyInfo keys = new BillingKeyInfo(
                previousPayment.getBillingKey(),
                previousPayment.getCustomerKey()
        );

        if (!isPossibleAutoBilling(keys)) {
            log.debug("빌링 키 유효성 검증 실패: {}", keys);
            return null;
        }

        log.debug("유효성 검증 성공한 주문 ID: {}", previousPayment.getOrderId());
        return PaymentDto.from(previousPayment);
    }

    public record BillingKeyInfo(String billingKey, String customerKey) {}

    /**
     * 구독 정보에 해당하는 회원의 최근 결제 정보를 조회합니다.
     *
     * @param memberId 결제 이력을 조회할 멤버의 고유 ID
     * @return 최근 결제 정보
     */
    public Payment getLastPayment(Long memberId) {
        return paymentQueryRepository.findLatestByMemberId(memberId).orElseThrow(
                () -> new ServiceException(PAYMENT_NOT_FOUND));
    }

    /**
     * 자동 결제 중단 조건 확인
     */
    public boolean isPossibleAutoBilling(BillingKeyInfo keys) {
        return StringUtils.hasText(keys.billingKey)
                && StringUtils.hasText(keys.customerKey);
    }

    /**
     * 새로운 결제 정보를 생성하고 트랜잭션 범위 내에서 데이터베이스에 저장합니다.
     * <p>
     * 처리 흐름:
     * 1. 이전 결제 정보의 유효성 검증
     * 2. 이전 결제 정보 기반으로 새 결제 객체 생성
     * 3. 영속화를 위한 데이터베이스 저장
     * </p>
     *
     * @param previousPayment 이전 결제 정보 (not null)
     * @return 저장된 새로운 결제 정보
     * @throws ServiceException 이전 결제 상태가 유효하지 않은 경우 발생
     */
    @Transactional
    public Payment createNewPaymentAndSave(PaymentDto previousPayment) {
        validatePaymentState(previousPayment);

        Payment newPayment = createNewPayment(previousPayment);
        updateNewPayment(previousPayment, newPayment);
        log.info("새 결제 생성 완료 - 주문 ID: {}, 회원 ID: {}",
                newPayment.getOrderId(), newPayment.getMemberId());
        return paymentRepository.save(newPayment); // 새로운 결제 정보를 저장
    }


    /**
     * 이전 결제 정보를 기반으로 새로운 결제 정보를 생성합니다.
     *
     * @param previousPayment 이전 결제 정보
     * @return 새로 생성된 결제 정보(Payment)
     */
    public Payment createNewPayment(PaymentDto previousPayment) {
        String newOrderId = paymentRequestService.generatedOrderId(previousPayment.getMemberId());
        return paymentRepository.save(buildPaymentEntity(previousPayment, newOrderId));
    }

    /**
     * 새 결제 정보에 이전 결제의 빌링 키와 고객 키를 복사하고,
     * 결제 상태를 '자동 결제 준비(AUTO_BILLING_PREPARED)'로 업데이트합니다.
     *
     * @param previousPayment 이전 결제 정보
     * @param newPayment      새로 생성된 결제 정보
     */
    public void updateNewPayment(PaymentDto previousPayment,
                                 Payment newPayment) {
        newPayment.updateBillingKeyAndCustomerKey(
                previousPayment.getBillingKey(),
                previousPayment.getCustomerKey()
        );
        paymentService.updatePaymentStatus(
                newPayment.getOrderId(),
                PayStatus.AUTO_BILLING_PREPARED
        );
    }

    /**
     * 결제 상태가 자동 결제 승인 또는 자동 결제 준비 상태인지 검증합니다.
     * 두 상태가 아닌 경우 ServiceException(INVALID_PAYMENT_STATE)를 발생시킵니다.
     *
     * @param previousPayment 이전 결제 정보
     * @throws ServiceException 결제 상태가 유효하지 않을 경우
     */
    public void validatePaymentState(PaymentDto previousPayment) {
        final Set<PayStatus> VALID_PAY_STATUSES = Set.of(
                PayStatus.AUTO_BILLING_READY,
                PayStatus.AUTO_BILLING_PREPARED
        );

        PayStatus currentStatus = previousPayment.getPayStatus();

        if (!VALID_PAY_STATUSES.contains(currentStatus)) {
            throw new ServiceException(INVALID_PAYMENT_STATE);
        }
        log.debug("이전 결제 상태 검증 성공: {}", currentStatus);
    }

    @Transactional // 새로운 트랜잭션에서 실행하는 작업
    public void handleExternalApiFailure(Payment newPayment,
                                         PaymentDto previousPayment,
                                         Exception e) {

        log.error("[구독 갱신 결제 실패 - 보상 로직 트리거] memberId: {}, orderId: {}, error: {}",
                newPayment.getMemberId(),
                newPayment.getOrderId(),
                e.getMessage(),
                e  // 스택 트레이스
        );

        if (!(e instanceof PaymentApiPortImpl.NonRetryableException)) {
            return; // NonRetryableException 일 경우 바로 보상 로직이 실행되어야 함
        }

        rollbackFailedPayment(newPayment, previousPayment);
        throw new ServiceException(BILLING_PAYMENT_FAIL, e);
    }

    /**
     * 구독 갱신 정보 저장 실패 시 보상 로직(결제 취소)을 실행합니다.
     * 새로운 트랜잭션에서 실행되어 기존 트랜잭션과 분리됩니다.
     *
     * @param description     구독 정보
     * @param newPayment      새로 생성된 결제 정보
     * @param previousPayment 이전 결제 정보
     * @param e               발생한 예외
     * @throws ServiceException BILLING_PAYMENT_FAIL 코드를 담은 예외 전파
     */
    @Transactional // 새로운 트랜잭션에서 실행하는 작업
    public void handleDbSaveFailed(String  description,
                                   Payment newPayment,
                                   PaymentDto previousPayment,
                                   Exception e) {
        log.error("[구독 갱신 정보 저장 실패 - 결제 취소 트리거] memberId: {}, orderId: {}, error: {}",
                newPayment.getMemberId(),
                newPayment.getOrderId(),
                e.getMessage(),
                e
        );

        // 결제 취소 API 호출 (멱등성 보장을 위해 customerKey를 idempotency key로 사용)
        // TODO: POST /v1/payments/{paymentKey}/cancel 구현 필요
        rollbackFailedPayment(newPayment, previousPayment); // 보상 트랜잭션

        log.info("[결제 취소 완료] memberId: {}, orderId: {}, 구독 플랜: {}, 금액: {}",
                newPayment.getMemberId(),
                newPayment.getOrderId(),
                description,
                newPayment.getTotalAmount()
        );

        throw new ServiceException(BILLING_PAYMENT_FAIL, e);
    }

    // 공통 로직
    /**
     * 결제 실패 시 보상 조치로 빌링 키 취소, 결제 상태 업데이트, 이벤트 발행, 구독 다운그레이드를 수행합니다.
     *
     * @param newPayment      실패한 새로운 결제 정보
     * @param previousPayment 이전 결제 정보
     * @throws ServiceException 결제 상태 업데이트 실패 시 발생
     */
    public void rollbackFailedPayment(Payment newPayment,
                                      PaymentDto previousPayment) {

        cancelBillingKeys(newPayment, previousPayment);
        updatePaymentStatusAndPublishEvent(newPayment);

        // 구독 정보 하락, 만료 설정
        subscriptionService.downgradeSubscriptionToBasic((newPayment.getMemberId()));
    }

    /**
     * 기존 및 신규 결제의 빌링 키와 고객 키를 취소합니다.
     */
    public void cancelBillingKeys(Payment newPayment, PaymentDto previousPayment) {
        log.debug("자동 결제 키 취소 시도: newOrderId={}, previousOrderId={}",
                newPayment.getOrderId(), previousPayment.getOrderId());
        cancelAutoBillingKey(newPayment.getOrderId());
        cancelAutoBillingKey(previousPayment.getOrderId());
    }

    public void cancelAutoBillingKey(String orderId) {
        Payment payment = findByOrderIdThrows(orderId);
        payment.updateBillingKeyAndCustomerKey(null, null);
    }

    /**
     * 결제 상태를 실패로 업데이트하고 이벤트를 발행합니다.
     */
    public void updatePaymentStatusAndPublishEvent(Payment newPayment) {
        paymentService.updatePaymentStatus(
                newPayment.getOrderId(),
                PayStatus.AUTO_BILLING_FAILED
        );
        publisher.publishEvent(new AutoBillingFailedEvent(newPayment.getId()));
    }

    /**
     * 편의 메서드
     */
    public Payment findByOrderIdThrows(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));
    }

    // 엔티티 빌드
    private Payment buildPaymentEntity(PaymentDto paymentDto, String orderId) {
        return Payment.builder()
                .memberId(paymentDto.getMemberId())
                .orderId(orderId)
                .plan(paymentDto.getPlan())
                .build();
    }
}
