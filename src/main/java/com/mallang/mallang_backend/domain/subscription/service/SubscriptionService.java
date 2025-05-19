package com.mallang.mallang_backend.domain.subscription.service;

import com.mallang.mallang_backend.domain.plan.entity.Plan;

import java.time.Clock;

public interface SubscriptionService {

    String getRoleName(Long memberId);
    boolean hasActiveSubscription(Long memberId);
    void updateSubscriptionInfo(Long memberId,
                                Plan plan,
                                Clock startDate);
    void updateIsAutoRenew(Long memberId);
    void downgradeSubscriptionToBasic(Long memberId);
    void updateSubscriptionStatus();
    void cancelSubscription(Long memberId);
}
