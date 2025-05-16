package com.mallang.mallang_backend.domain.payment.service.common;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.request.BillingPaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.confirm.PaymentApiPort;
import com.mallang.mallang_backend.domain.payment.service.confirm.PaymentConfirmService;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.PAYMENT_NOT_FOUND;

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

    public MemberGrantedInfo getMemberId(String orderId) {
        Payment payment = findByOrderIdThrows(orderId);
        return new MemberGrantedInfo(
                payment.getMemberId(),
                payment.getPlan().getType().getRoleName());
    }

    public record MemberGrantedInfo(Long memberId, String roleName) {
    }

    public void checkIdemkeyAndSave(String idempotencyKey
    ) {
        redisService.checkIdemkeyAndSave(idempotencyKey);
    }

    @Transactional
    public void updatePaymentStatus(String orderId,
                                    PayStatus changedPayStatus
    ) {
        Payment payment = findByOrderIdThrows(orderId);
        payment.updatePayStatus(changedPayStatus);

        // 결제 히스토리 업데이트 이벤트 발행
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

    // ========== 자동 결제 메서드 =========

    // 1. 토스 API 로 요청을 보내서 빌링 키를 발급 받기
    // TODO orderId 와 billingKey 를 redis 에 저장하고 중복 요청을 거절하기
    public String issueBillingKey(BillingPaymentRequest request
    ) {
        return paymentApiPort.callTossPaymentBillingAPI(request);
    }

    // 2. 빌링 키 - 고객 키를 DB에 업데이트 (이후 자동 결제 시에 이용할 값)
    @Transactional
    public void updateBillingKeyAndCustomerKey(String OrderId,
                                               String billingKey,
                                               String customerKey
    ) {
        Payment payment = paymentRepository.findByOrderId(OrderId).orElseThrow(
                () -> new ServiceException(PAYMENT_NOT_FOUND));

        payment.updateBillingKeyAndCustomerKey(billingKey, customerKey);
        log.debug("[자동 결제 키 저장] billingKey: {}, customerKey: {}", billingKey, customerKey);
    }

    // 3. 발급 받은 빌링 키로 자동으로 결제 시작 -> 응답 반환 (성공 혹 실패)
    public BillingPaymentResponse sendApproveAutoBillingRequest(BillingPaymentRequest request,
                                                                String billingKey
    ) {
        return paymentApiPort.payWithBillingKey(billingKey,
                request
        );
    }

    // 4. 받은 응답을 DB에 저장 및 구독 설정 업데이트
    @Transactional
    public void processAutoBillingPaymentResult(BillingPaymentResponse approveResponse
    ) {
        paymentConfirmService.processAutoBillingPaymentResult(approveResponse);
    }

    // 유저 ID 로 결제 정보 가져오기
    public String getPaymentByMemberId(Long memberId) {
        return paymentRepository.findByMemberId(memberId).getLast().getOrderId();
    }
}
