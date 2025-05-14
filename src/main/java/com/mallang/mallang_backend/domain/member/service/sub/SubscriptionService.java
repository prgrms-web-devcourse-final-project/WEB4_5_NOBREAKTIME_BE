package com.mallang.mallang_backend.domain.member.service.sub;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.plan.entity.Plan;

import java.time.LocalDateTime;

public interface SubscriptionService {

    String getRoleName(Long memberId);
    boolean hasActiveSubscription(Long memberId);
    void updateSubscriptionType(Long memberId, SubscriptionType subscriptionType);

    // 구독 엔티티 업데이트
    void updateSubscriptionInfo(Long memberId,
                                Plan plan,
                                LocalDateTime startDate);
}
