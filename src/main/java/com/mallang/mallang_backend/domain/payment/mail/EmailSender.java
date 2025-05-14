package com.mallang.mallang_backend.domain.payment.mail;

public interface EmailSender {

    void sendVerificationEmail(String email, String url);
}
