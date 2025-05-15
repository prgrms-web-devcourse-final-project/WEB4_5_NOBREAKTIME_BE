package com.mallang.mallang_backend.domain.payment.mail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MailSenderIntegrationTest {

    @Autowired
    private EmailSender emailSender;

    @Test
    @Disabled("실제 메일 전송 테스트 - 본인 이메일 입력 후 전송")
    void actual_send_email() {
        // Given
        String email = ""; // 실제 이메일로 변경
        String verificationUrl = "https://mallang.com/verify/1234";

        // When & Then (예외가 발생하지 않으면 성공)
        emailSender.sendVerificationEmail(email, verificationUrl);
    }
}