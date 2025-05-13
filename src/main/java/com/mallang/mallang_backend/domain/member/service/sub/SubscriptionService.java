package com.mallang.mallang_backend.domain.member.service.sub;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;

public interface SubscriptionService {

    String getRoleName(Long memberId);

    boolean hasActiveSubscription(Long memberId);
    void updateSubscription(Long memberId, SubscriptionType subscriptionType);
}
