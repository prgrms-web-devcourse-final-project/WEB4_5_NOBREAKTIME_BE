package com.mallang.mallang_backend.domain.payment.entity;

public enum PayPlatform {
    CARD,           // 신용/체크카드
    ACCOUNT_TRANSFER, // 계좌이체
    VIRTUAL_ACCOUNT,  // 가상계좌(무통장입금)
    PHONE,          // 휴대폰 결제
    EASY_PAY,       // 간편결제(카카오페이, 토스 등)
    POINT,          // 포인트 결제
    KAKAO_PAY,      // 카카오페이
    TOSS_PAY,       // 토스페이
    NAVER_PAY,      // 네이버페이
    APPLE_PAY,      // 애플페이
    GOOGLE_PAY      // 구글페이
}