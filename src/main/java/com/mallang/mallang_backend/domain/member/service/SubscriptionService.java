package com.mallang.mallang_backend.domain.member.service;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;

public interface SubscriptionService {

    boolean hasActiveSubscription(Long memberId);
    void updateSubscription(Long memberId, SubscriptionType subscriptionType);
}
