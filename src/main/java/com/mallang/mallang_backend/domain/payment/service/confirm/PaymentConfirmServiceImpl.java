package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.event.dto.PaymentFailedEvent;
import com.mallang.mallang_backend.domain.payment.event.dto.PaymentMailSendEvent;
import com.mallang.mallang_backend.domain.payment.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.domain.payment.thirdparty.PaymentApiPort;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 결제 승인 및 결과 처리 서비스 구현체입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentConfirmServiceImpl implements PaymentConfirmService {

    private final PaymentRedisService redisService;
    private final PaymentRepository paymentRepository;
    private final PaymentApiPort paymentApiPort;
    private final SubscriptionService subscriptionService;
    private final ApplicationEventPublisher publisher;

    // ========== 공통 메서드 추출 ========== //

    /**
     * 결제 성공 시 공통 처리 (이벤트 발행 + 구독 업데이트)
     */
    private void handleCommonSuccessActions(Payment payment,
                                            LocalDateTime approvedAt,
                                            String receiptUrl) {

        // 상태값 분기 처리
        PayStatus status = (payment.getBillingKey() != null)
                ? PayStatus.AUTO_BILLING_APPROVED
                : PayStatus.DONE;

        // 이벤트 발행
        publisher.publishEvent(new PaymentUpdatedEvent(
                payment.getId(),
                status,
                status.getDescription()
        ));

        publisher.publishEvent(new PaymentMailSendEvent(
                payment.getId(),
                payment.getMemberId(),
                receiptUrl
        ));
    }

    /**
     * 결제 실패 시 공통 처리
     */
    private void handleCommonFailureActions(Payment payment,
                                            String paymentKey,
                                            String code,
                                            String message) {
        payment.updateFailInfo(message);
        publisher.publishEvent(new PaymentFailedEvent(
                payment.getId(),
                code,
                message
        ));
        log.error("[결제실패] orderId: {}", payment.getOrderId());
    }

    /**
     * ApprovedAt 변환 로직 통합
     */
    private LocalDateTime createApprovedTime(String approvedAt) {
        return OffsetDateTime.parse(approvedAt).toLocalDateTime();
    }

    /**
     * 결제 승인 요청을 외부 API로 전송하고 응답 객체를 반환합니다.
     *
     * @param request 결제 승인 요청 정보
     * @return 결제 승인 응답 정보
     * @throws ServiceException 요청 값 검증 실패 시 예외 발생
     */
    @Override
    public PaymentResponse sendApproveRequest(PaymentApproveRequest request) {
        // 1. 결제 요청 값이 정확한지 검증 (Redis 체크)
        validatePaymentRequest(request);

        // 2. 외부 API 호출
        return paymentApiPort.callTossPaymentAPI(request);
    }

    /**
     * 결제 결과를 처리하여 결제 상태를 업데이트합니다.
     *
     * @param orderId 주문 ID
     * @param result  결제 승인 응답 정보
     * @throws ServiceException 결제 정보가 존재하지 않을 경우 예외 발생
     */
    // 반환된 값을 가지고 결제 결과를 업데이트
    public void processPaymentResult(String orderId, PaymentResponse result) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new ServiceException(PAYMENT_NOT_FOUND));

        if (result.getStatus().equals("DONE")) {
            payment.updateSuccessInfo(
                    result.getPaymentKey(),
                    createApprovedTime(result.getApprovedAt()),
                    result.getMethod()); // 결제 성공 내역 업데이트

            log.debug("[결제 성공] paymentKey: {}, approvedAt: {}",
                    payment.getPaymentKey(), result.getApprovedAt());

            // 구독 정보 업데이트
            subscriptionService.updateSubscriptionInfo(
                    payment.getMemberId(),
                    payment.getPlan(),
                    Clock.systemDefaultZone()
            );

            handleCommonSuccessActions(
                    payment,
                    createApprovedTime(result.getApprovedAt()),
                    result.getReceipt().getUrl()
            ); // 결제 성공
        } else {
            payment.updateFailInfo(
                    result.getFailure().getMessage()
            ); // DB 업데이트
            handleCommonFailureActions(
                    payment,
                    result.getPaymentKey(),
                    result.getFailure().getCode(),
                    result.getFailure().getMessage()
            ); // 결제 승인 실패
        }
    }

    /**
     * 결제 요청의 유효성을 검증합니다. (Redis 에서 주문 ID와 금액 확인)
     *
     * @param request 결제 승인 요청 정보
     * @throws ServiceException 유효성 검증 실패 시 예외 발생
     */
    private void validatePaymentRequest(PaymentApproveRequest request) {
        redisService.checkOrderIdAndAmount(request.getOrderId(), request.getAmount());
    }

    // ========== 자동 결제 메서드 =========

    @Override
    @Retry(name = "retryDbSave", fallbackMethod = "retryDbSaveFallback")
    @Transactional
    public void processAutoBillingPaymentResult(BillingPaymentResponse response) {

        Payment payment = paymentRepository.findByOrderId(response.getOrderId())
                .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));

        String paymentKey = response.getPaymentKey();

        if (response.getStatus().equals("DONE")) {

            payment.updateBillingSuccessInfo(
                    createApprovedTime(response.getApprovedAt()),
                    response.getMethod(),
                    paymentKey
            ); // 결제 성공 내역 업데이트


            renewalSubscription(payment); // 구독 업데이트 + 갱신
            handleCommonSuccessActions(
                    payment,
                    createApprovedTime(response.getApprovedAt()),
                    response.getReceipt().getUrl()
            ); // 결제 성공
        } else {
            subscriptionService.downgradeSubscriptionToBasic(payment.getMemberId());
            handleCommonFailureActions(
                    payment,
                    response.getFailure().getCode(),
                    response.getFailure().getMessage(),
                    paymentKey
            ); // 결제 승인 실패
        }
    }

    /**
     * 구독 일자 기준
     * 3월 15일 4시 구독 -> 4월 14일 4시에 결제 시작 (구독 자체는 14일까지 사용이 가능)
     * 구독 만료는 15일, 15일부터 새로운 구독 사용 가능
     */
    private void renewalSubscription(Payment payment) {
        // 오늘 날짜 기준 +1일 후를 기준으로 Clock 으로 생성 (구독 갱신은 하루 후)
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.now(zone).plusDays(1);

        // LocalDateTime → ZonedDateTime → Instant 변환
        Clock tomorrowClock = Clock.fixed(localDateTime.atZone(zone).toInstant(), zone);

        subscriptionService.updateSubscriptionInfo(
                payment.getMemberId(),
                payment.getPlan(),
                tomorrowClock
        );

        subscriptionService.updateIsAutoRenew(payment.getMemberId());
    }

    public void retryDbSaveFallback(BillingPaymentResponse response,
                                    Exception e) {
        if (e instanceof PessimisticLockingFailureException) {
            throw new ServiceException(PAYMENT_CONFLICT, e);
        }
        // TODO 보상 로직을 생각할 것
        log.error("DB 저장 실패: {}", e.getMessage());
        throw new ServiceException(PAYMENT_CONFIRM_FAIL, e);
    }
}