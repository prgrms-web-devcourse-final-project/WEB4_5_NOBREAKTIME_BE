package com.mallang.mallang_backend.domain.plan.entity.domain.subscription.service;

import com.mallang.mallang_backend.domain.plan.entity.domain.plan.entity.Plan;

import java.time.Clock;

public interface SubscriptionService {

    String getRoleName(Long memberId);
    boolean hasActiveSubscription(Long memberId);
    // 구독 테이블 업데이트
    void updateSubscriptionInfo(Long memberId,
                                Plan plan,
                                Clock startDate);

    void updateIsAutoRenew(Long memberId);

    void downgradeSubscriptionToBasic(Long memberId);

    void cancelSubscription(Long memberId);
}
