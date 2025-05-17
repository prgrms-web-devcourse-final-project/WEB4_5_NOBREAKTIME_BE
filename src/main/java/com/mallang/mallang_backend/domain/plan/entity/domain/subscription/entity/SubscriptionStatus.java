package com.mallang.mallang_backend.domain.plan.entity.domain.subscription.entity;

public enum SubscriptionStatus {
    ACTIVE, // 구독 중
    CANCELED, // 구독 취소 (구독 갱신 전 취소, 사용 일자 남음)
    EXPIRED // 구독 만료
}