package com.mallang.mallang_backend.domain.payment.service.event;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.history.PaymentHistory;
import com.mallang.mallang_backend.domain.payment.history.PaymentHistoryQueryRepository;
import com.mallang.mallang_backend.domain.payment.history.PaymentHistoryRepository;
import com.mallang.mallang_backend.domain.payment.mail.MailSender;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentFailedEvent;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentMailSendEvent;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PaymentHistoryRepository historyRepository;
    private final PaymentHistoryQueryRepository historyQueryRepository;
    private final MailSender mailSendService;

    @Async("PaymentCompletedExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handleSaveFailedHistory(PaymentFailedEvent event) {
        Payment payment = getPaymentOrThrow(event.getPaymentId());

        saveHistory(payment, PayStatus.ABORTED, event.getMessage()); // 실패 히스토리
        log.error("[PaymentLog-Failed] 승인 실패 내역 저장 완료 orderId: {}, code: {}, reason: {}",
                payment.getId(), event.getCode(), event.getMessage());
    }

    @Async("PaymentCompletedExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handleUpdatedHistory(PaymentUpdatedEvent event) {
        Payment payment = getPaymentOrThrow(event.getPaymentId());

        PayStatus lastStatus = historyQueryRepository.findByPaymentLastStatus(payment.getId());
        String beforeStatus = (lastStatus != null) ? lastStatus.name() : "NONE";

        saveHistory(payment, event.getStatus(), event.getMessage());
        log.info("[PaymentLog-Update] 결제 내역 업데이트 orderId: {} | before -> after: {} -> {}, reason: {}",
                payment.getId(), beforeStatus, event.getStatus(), event.getMessage());
    }

    @Async("PaymentCompletedExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handleSendMail(PaymentMailSendEvent event) {

        sendPaymentEmail(event.getPaymentId(), event.getReceiptUrl()); // 이메일 전송
    }

    private void sendPaymentEmail(Long paymentId, String receiptUrl) {
        Payment payment = getPaymentOrThrow(paymentId);
        Member member = memberRepository.findById(payment.getMemberId())
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        try {
            mailSendService.sendVerificationEmail(member.getEmail(), receiptUrl);
            log.debug("[PaymentLog-Success] 결제 완료 이메일 발송 성공: {}", member.getEmail());
        } catch (Exception e) {
            log.error("[PaymentLog-Success] 이메일 발송 실패: {}", e.getMessage(), e);
        }
    }

    // 결제 조회 공통 로직
    private Payment getPaymentOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    // 결제 히스토리 저장 공통 로직
    private void saveHistory(Payment payment, PayStatus status, String reason) {
        PaymentHistory history = PaymentHistory.builder()
                .payment(payment)
                .status(status)
                .reasonDetail(reason)
                .build();

        historyRepository.save(history);
    }
}
