package com.mallang.mallang_backend.domain.payment.service.event;

import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.history.PaymentHistoryRepository;
import com.mallang.mallang_backend.domain.payment.mail.MailSender;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentFailedEvent;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentMailSendEvent;
import com.mallang.mallang_backend.domain.payment.service.event.dto.PaymentUpdatedEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
class PaymentEventListenerTest {

    @Autowired
    private PaymentEventListener paymentEventListener;

    @Autowired
    private PaymentHistoryRepository historyRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private MailSender mailSendService;

    @Test
    @DisplayName("결제 성공 시 비동기 처리 검증")
    void pay_updateSuccessInfo_and(CapturedOutput output) throws InterruptedException {
        // Given
        PaymentUpdatedEvent event = new PaymentUpdatedEvent(
                1L,
                PayStatus.DONE,
                PayStatus.DONE.getDescription());

        PaymentMailSendEvent mailEvent = new PaymentMailSendEvent(
                1L,
                1L,
                "test@url.com"
        );

        // When
        paymentEventListener.handleUpdatedHistory(event);
        paymentEventListener.handleSendMail(mailEvent);
        Thread.sleep(2000); // 비동기 작업 완료 대기

        // Then
        verify(mailSendService, times(1))
                .sendVerificationEmail(anyString(), anyString());

        // 로그에서 스레드 이름 확인
        assertThat(output.getOut())
                .contains("Payment-Async-") // 스레드 풀 접두사 확인
                .contains("[PaymentLog-Success] 결제 완료 이메일 발송 성공: user1@gmail.com")
                .contains("[PaymentLog-Update] 결제 내역 업데이트");
    }

    @Test
    @DisplayName("결제 실패 시 비동기로 실패 내역을 저장함")
    void pay_failed_and(CapturedOutput output) throws InterruptedException {
        // Given
        String errorCode = "NOT_FOUND_PAYMENT";
        String errorMessage = "존재하지 않는 결제 입니다.";

        Payment failedPayment = paymentRepository.findById(2L).get(); //실패한 경우 (미리 DB 저장)

        PaymentFailedEvent event = new PaymentFailedEvent(
                failedPayment.getId(),
                errorCode,
                errorMessage
        );

        // When
        paymentEventListener.handleSaveFailedHistory(event);
        Thread.sleep(2000); // 비동기 작업 완료 대기

        // Then: 히스토리 저장이 되었는지 확인
        assertThat(historyRepository.count()).isGreaterThan(1);

        // 로그에서 스레드 이름 확인
        assertThat(output.getOut())
                .contains("Payment-Async-") // 스레드 풀 접두사 확인
                .contains("[PaymentLog-Failed]");
    }
}