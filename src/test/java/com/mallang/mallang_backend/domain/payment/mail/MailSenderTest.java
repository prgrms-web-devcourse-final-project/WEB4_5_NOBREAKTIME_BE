package com.mallang.mallang_backend.domain.payment.mail;

import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    private MailSender mailSender;

    @BeforeEach
    void setUp() {
        mailSender = new MailSender(javaMailSender, "noreply@mallang.com");
    }

    @Test
    @DisplayName("메일 전송 성공 테스트")
    void sendVerificationEmail() throws Exception {
        //given
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);

        //when
        mailSender.sendVerificationEmail("test@example.com", "https://mallang.com/verify");
        
        //then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }
    
    @Test
    @DisplayName("send() 단계에서 메일 전송 실패")
    void sendVerificationEmail_fail() throws Exception {
        //given
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);

        // send()에서 MailException 발생시키기
        doThrow(new MailSendException("Test Exception"))
                .when(javaMailSender)
                .send(any(MimeMessage.class));

        //when & then
        assertThrows(ServiceException.class, () ->
                mailSender.sendVerificationEmail("test@example.com", "https://mallang.com/verify")
        );
    }
}