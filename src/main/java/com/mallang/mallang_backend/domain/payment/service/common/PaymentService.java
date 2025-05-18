
package com.mallang.mallang_backend.domain.payment.service.common;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.request.BillingPaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.confirm.PaymentApiPort;
import com.mallang.mallang_backend.domain.payment.service.confirm.PaymentConfirmService;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRequestService;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;
    private final PaymentRedisService redisService;
    private final PaymentConfirmService paymentConfirmService;
    private final PaymentApiPort paymentApiPort;
    private final SubscriptionService subscriptionService;
    private final PaymentRequestService paymentRequestService;

    // ============== 기본 결제 로직 =================== //
    @Transactional // 보상 처리 로직이 필요함
    public MemberGrantedInfo processPaymentAndUpdateSubscription(PaymentApproveRequest request) {
        try {
            // 1. 멱등성 키 검증 및 저장 (트랜잭션 내)
            redisService.checkAndSaveIdempotencyKey(request.getIdempotencyKey());

            // 2. 결제 요청 값이 정확한지 검증 (Redis 체크)
            redisService.checkOrderIdAndAmount(request.getOrderId(), request.getAmount());

            // 3. 결제 상태 IN_PROGRESS로 업데이트 (트랜잭션 내)
            Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));

            updatePaymentStatus(request.getOrderId(), PayStatus.IN_PROGRESS);

            // 4. 외부 결제 승인 API 호출 (트랜잭션 밖)
            PaymentResponse response = paymentApiPort.callTossPaymentAPI(request);

            // 5. 결제 결과 처리 (트랜잭션 내)
            paymentConfirmService.processPaymentResult(request.getOrderId(), response);

            // 5. 멤버 권한 정보 조회 (트랜잭션 내)
            return new MemberGrantedInfo(
                    payment.getMemberId(),
                    payment.getPlan().getType().getRoleName()
            );
        } catch (Exception e) {
            log.error("[결제 실패] orderId: {}", request.getOrderId());
            // 결제 실패 시 결제 상태 변경
            throw new ServiceException(PAYMENT_CONFIRM_FAIL, e);
        }
    }

    @Transactional
    public PaymentRequest createPaymentRequest(Long memberId,
                                               PaymentSimpleRequest simpleRequest) {
        return paymentRequestService.createPaymentRequest(memberId, simpleRequest);
    }

    // ============= 자동 결제 메서드 ============ //

    // 빌링 키를 발급하는 로직
    @Transactional
    public void processIssueBillingKey(BillingPaymentRequest request) {
        try {
            // 1. 빌링 키 발급 (외부 API) (트랜잭션 밖)
            String billingKey = paymentApiPort.issueBillingKey(
                    request.getCustomerKey(),
                    request.getAuthKey(),
                    request.getOrderId()
            );

            // 2. 고객 정보 업데이트 (DB) - 빌링 키, 고객 키 업데이트
            updateBillingKeyAndCustomerKey(
                    request.getOrderId(),
                    billingKey,
                    request.getCustomerKey()
            );

            Payment payment = findByOrderIdThrows(request.getOrderId());

            // 3. 구독 상태 변경 (DB)
            subscriptionService.updateIsAutoRenew(payment.getMemberId());
        } catch (Exception e) {
            log.error("[구독 갱신 빌링 키 발급 실패] orderId: {}", request.getOrderId());
            throw new ServiceException(BILLING_KEY_ISSUE_FAILED, e);
        }
    }

    @Transactional
    public void cancelSubscription(Long memberId) {
        subscriptionService.cancelSubscription(memberId);
    }

    // ============== 공통 결제 실패 로직 =========== //
    @Transactional
    public void handleFailedPayment(String orderId, String errorCode) {
        // 1. 결제 내역 조회 (트랜잭션 내)
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));

        // 2. 결제 상태 업데이트 (트랜잭션 내)
        payment.updateStatus(PayStatus.ABORTED);

        // 3. 결제 상태 업데이트 (에러 코드)
        payment.updateFailInfo(errorCode);
    }

    // ============ 프라이빗 메서드 =========== //

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
     * 주문 ID에 해당하는 결제 정보에 빌링 키와 고객 키를 업데이트합니다.
     * 트랜잭션 내에서 실행되며 영속성 컨텍스트가 자동으로 관리됩니다.
     *
     * @param orderId     주문 ID
     * @param billingKey  발급받은 빌링 키
     * @param customerKey 발급받은 고객 키
     */
    private void updateBillingKeyAndCustomerKey(String orderId,
                                                String billingKey,
                                                String customerKey) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));

        payment.updateBillingKeyAndCustomerKey(billingKey, customerKey);

        updatePaymentStatus(orderId, PayStatus.AUTO_BILLING_READY); // 변경 여부 고려

        log.debug("[자동 결제 키 저장] billingKey: {}, customerKey: {}",
                billingKey, customerKey);
    }

    /**
     * 주문 ID로 결제 정보를 조회합니다. 존재하지 않으면 ServiceException을 발생시킵니다.
     *
     * @param orderId 주문 ID
     * @return 결제 정보
     * @throws ServiceException 결제 정보가 없을 경우
     */
    private Payment findByOrderIdThrows(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));
    }

    /**
     * 회원 ID와 권한 이름 정보를 담는 레코드입니다.
     *
     * @param memberId 회원 ID
     * @param roleName 권한 이름
     */
    public record MemberGrantedInfo(Long memberId, String roleName) {
    }
}
