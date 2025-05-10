package com.mallang.mallang_backend.domain.subscription.entity;

public enum SubscriptionStatus {
    ACTIVE, // 구독 중
    CANCELLED, // 구독 취소 (구독 갱신 전 취소, 사용 일자 남음)
    EXPIRED // 구독 만료
}
