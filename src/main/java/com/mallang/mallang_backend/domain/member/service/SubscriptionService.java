package com.mallang.mallang_backend.domain.member.service;

import com.mallang.mallang_backend.domain.member.entity.Subscription;

public interface SubscriptionService {

    boolean hasActiveSubscription(Long memberId);
    void updateSubscription(Long memberId, Subscription subscription);
}
