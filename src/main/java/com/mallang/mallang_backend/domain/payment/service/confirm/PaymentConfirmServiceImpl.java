package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentFailedEvent;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentMailSendEvent;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.mallang.mallang_backend.domain.payment.entity.PayStatus.DONE;
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
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));

        if (result.getStatus().equals("DONE")) {
            handleApprovalSuccess(payment, result); // 결제 성공
        } else {
            handleApprovalFailure(payment, result); // 결제 승인 실패
        }
    }

    /**
     * 결제 승인 실패를 처리하는 메서드입니다.
     * 결제 엔티티를 실패 상태로 업데이트하고, 결제 실패 이벤트를 발행해 로그를 기록합니다.
     *
     * @param payment 결제 엔티티
     * @param result  결제 승인 응답 정보
     * @throws ServiceException 결제 승인 실패 시 예외 발생
     */
    private void handleApprovalFailure(Payment payment, PaymentResponse result) {
        payment.updateFailInfo( // 결제 실패 상태로 업데이트
                result.getPaymentKey(), result.getFailure().getCode());
        publisher.publishEvent(new PaymentFailedEvent( // 결제 실패 이벤트 발생
                payment.getId(), result.getFailure().getCode(), result.getFailure().getMessage()));
        log.error("[결제승인실패] orderId: {}, paymentKey: {}, status: {}", payment.getOrderId(), result.getPaymentKey(), result.getStatus());

        throw new ServiceException(PAYMENT_CONFIRM_FAIL);
    }

    /**
     * 결제 승인 성공을 처리하는 메서드입니다.
     * 결제 엔티티를 성공 상태로 업데이트하고, 결제 성공 이벤트를 발행하며, 구독 정보를 갱신합니다.
     *
     * @param payment 결제 엔티티
     * @param result  결제 승인 응답 정보
     */
    private void handleApprovalSuccess(Payment payment, PaymentResponse result) {
        payment.updateSuccessInfo(result.getPaymentKey(), createApprovedTime(result), result.getMethod());
        String receiptUrl = result.getReceipt().getUrl();
        publisher.publishEvent(new PaymentUpdatedEvent( // 결제 성공 이벤트 발생
                payment.getId(), DONE, DONE.getDescription()));
        publisher.publishEvent(new PaymentMailSendEvent( // 메일 발송 이벤트 발생
                payment.getId(), payment.getMemberId(), receiptUrl));
        subscriptionService.updateSubscriptionInfo( // 구독 업데이트
                payment.getMemberId(), payment.getPlan(), createApprovedTime(result));
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

    /**
     * 결제 승인 시각을 ISO 에서 LocalDateTime 으로 변환합니다.
     *
     * @param result 결제 승인 응답 정보
     * @return 승인 시각(LocalDateTime)
     * @throws NullPointerException result 또는 승인 시각이 null인 경우
     */
    private LocalDateTime createApprovedTime(PaymentResponse result) {
        String approvedAtToString = Objects.requireNonNull(result).getApprovedAt();
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(approvedAtToString);
        return offsetDateTime.toLocalDateTime();
    }

    // ========== 자동 결제 메서드 =========

    // 빌링 키를 통해 반환된 값을 가지고 결제 결과 업데이트 및 이벤트를 발행
    @Override
    public void processAutoBillingPaymentResult(BillingPaymentResponse response
    ) {
        Payment payment = paymentRepository.findByOrderId(response.getOrderId())
                .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));

        String paymentKey = response.getPaymentKey();

        if (response.getStatus().equals("DONE")) {
            handleBillingApprovalSuccess(payment, response); // 결제 성공
        } else {
            handleBillingApprovalFailure(payment,
                    response.getFailure().getCode(),
                    response.getFailure().getMessage(),
                    paymentKey); // 결제 승인 실패
        }
    }

    private void handleBillingApprovalSuccess(Payment payment,
                                              BillingPaymentResponse response
    ) {
        payment.updateBillingSuccessInfo(
                createApprovedTime(response.getApprovedAt()),
                response.getMethod(),
                response.getPaymentKey()
        ); // 객체 업데이트 (결제 성공)

        subscriptionService.updateSubscriptionInfo( // 구독 업데이트
                payment.getMemberId(),
                payment.getPlan(),
                createApprovedTime(response.getApprovedAt())
        );
        subscriptionService.updateIsAutoRenew(payment.getMemberId());

        publisher.publishEvent(new PaymentUpdatedEvent( // 구독 갱신 성공 이벤트 발생
                payment.getId(),
                DONE,
                DONE.getDescription()
        ));

        publisher.publishEvent(new PaymentMailSendEvent( // 메일 발송 이벤트 발생
                payment.getId(),
                payment.getMemberId(),
                response.getReceipt().getUrl()
        ));
    }

    private void handleBillingApprovalFailure(Payment payment,
                                              String paymentKey,
                                              String code,
                                              String message
    ) {

        payment.updateFailInfo(
                paymentKey,
                message
        ); // DB 업데이트

        publisher.publishEvent(new PaymentFailedEvent( // 구독 갱신 실패 이벤트 발생
                payment.getId(),
                code,
                message
        ));

        // 구독 업데이트 -> 실패하면 BASIC 으로 돌아가기
        subscriptionService.downgradeSubscriptionToBasic(payment.getMemberId());

        throw new ServiceException(BILLING_PAYMENT_FAIL);
    }

    private LocalDateTime createApprovedTime(String approvedAt) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(approvedAt);
        return offsetDateTime.toLocalDateTime();
    }


}