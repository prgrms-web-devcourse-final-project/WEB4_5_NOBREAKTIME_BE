package com.mallang.mallang_backend.domain.payment.mail;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class MailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final String senderEmail;

    public MailSender(JavaMailSender javaMailSender,
                      @Value("${spring.mail.username}") String senderEmail
    ) {
        this.javaMailSender = javaMailSender;
        this.senderEmail = senderEmail;
    }

    @Retryable(value = MailSendException.class, maxAttempts = 5, // 5번 재시도
            backoff = @Backoff(delay = 10000) // 재시도 간격 10초 고정
    )
    public void sendVerificationEmail(String email, String url) {
        try {
            MimeMessage message = createMimeMessage(email, url);
            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.warn("메일 발송 실패 - 수신자: {}", email);
            throw new ServiceException(ErrorCode.EMAIL_SEND_FAILED, e);
        }
    }

    private MimeMessage createMimeMessage(String email, String url) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.addInline("logo", new File("../upload/img.png"));

        helper.setFrom(senderEmail);
        helper.setTo(email);
        helper.setSubject("[Mallang] 플랜 가입 완료");
        helper.setText(buildEmailTemplate(url), true);

        return message;
    }

    private String buildEmailTemplate(String planManageUrl) {
        return String.format("""
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <title>Mallang 플랜 가입 완료</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                @import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css');
                body {
                    margin: 0;
                    padding: 0;
                    background: #F5F2FF;
                    font-family: 'Pretendard', 'Apple SD Gothic Neo', sans-serif;
                }
                .container {
                    max-width: 600px;
                    margin: 40px auto;
                    background: white;
                    border-radius: 24px;
                    box-shadow: 0 8px 32px rgba(82,17,159,0.08);
                    overflow: hidden;
                }
                .header {
                    background: linear-gradient(135deg, #E6E6FA 0%%, #F8F4FF 100%%);
                    padding: 48px 0 32px 0;
                    text-align: center;
                    position: relative;
                }
                .header::after {
                    content: "";
                    position: absolute;
                    bottom: -20px;
                    left: 0;
                    right: 0;
                    height: 40px;
                    background: url('data:image/svg+xml,<svg viewBox="0 0 1200 120" xmlns="http://www.w3.org/2000/svg"><path fill="%%23ffffff" d="M0 46L1200 0V120H0z"/></svg>');
                }
                .logo {
                    width: 100px;
                    height: auto;
                    margin-bottom: 24px;
                }
                .header-title {
                    color: #6A5ACD;
                    font-size: 32px;
                    font-weight: 800;
                    margin: 16px 0;
                    letter-spacing: -0.5px;
                }
                .content {
                    padding: 48px 40px;
                    color: #4A4A4A;
                }
                .highlight {
                    color: #6A5ACD;
                    font-weight: 700;
                }
                .cta-button {
                    display: inline-block;
                    background: linear-gradient(45deg, #9370DB, #6A5ACD);
                    color: white !important;
                    padding: 16px 40px;
                    margin: 32px 0;
                    border-radius: 12px;
                    text-decoration: none;
                    font-weight: 600;
                    font-size: 18px;
                    box-shadow: 0 4px 16px rgba(106,90,205,0.2);
                    transition: transform 0.2s;
                }
                .cta-button:hover {
                    transform: translateY(-2px);
                }
                .feature-list {
                    margin: 24px 0;
                    padding: 0;
                    list-style: none;
                }
                .feature-item {
                    padding: 16px;
                    margin: 12px 0;
                    background: #F9F8FF;
                    border-radius: 8px;
                    display: flex;
                    align-items: center;
                }
                .feature-icon {
                    width: 24px;
                    height: 24px;
                    margin-right: 12px;
                    filter: invert(40%%) sepia(90%%) saturate(500%%) hue-rotate(220deg);
                }
                .contact {
                    margin-top: 40px;
                    text-align: center;
                }
                .contact a {
                    color: #9370DB;
                    text-decoration: none;
                    font-weight: 500;
                }
                .footer {
                    background: #F5F2FF;
                    padding: 24px;
                    text-align: center;
                    font-size: 12px;
                    color: #888;
                }
                @media (max-width: 640px) {
                    .container {
                        margin: 20px;
                        border-radius: 16px;
                    }
                    .content {
                        padding: 32px 24px;
                    }
                    .header-title {
                        font-size: 28px;
                    }
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <img src="cid:logo" class="logo" alt="Mallang 로고">
                    <h1 class="header-title">환영합니다! 🎉</h1>
                </div>
                
                <div class="content">
                    <p>안녕하세요, <span class="highlight">Mallang 프리미엄</span> 회원이 되신 것을 진심으로 축하드립니다!</p>
                    
                    <a href="%s" class="cta-button">지금 바로 학습 시작하기 →</a>

                    <ul class="feature-list">
                        <li class="feature-item">
                            <img src="cid:feature1" class="feature-icon" alt="아이콘">
                            <span>AI 맞춤형 학습 경로 제공</span>
                        </li>
                        <li class="feature-item">
                            <img src="cid:feature2" class="feature-icon" alt="아이콘">
                            <span>실시간 발음 분석 기능</span>
                        </li>
                        <li class="feature-item">
                            <img src="cid:feature3" class="feature-icon" alt="아이콘">
                            <span>프리미엄 콘텐츠 무제한 이용</span>
                        </li>
                    </ul>

                    <div class="contact">
                        <p>궁금한 점이 있으신가요?<br>
                        <a href="mailto:help@mallang.com">고객지원팀에 문의하기</a></p>
                    </div>
                </div>

                <div class="footer">
                    © 2025 Mallang. All rights reserved.<br>
                    본 메일은 발신전용입니다.
                </div>
            </div>
        </body>
        </html>
        """, planManageUrl);
    }
}