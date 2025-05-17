package com.mallang.mallang_backend.domain.plan.entity.domain.subscription.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.plan.entity.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<List<Subscription>> findByMember(Member member);
    List<Subscription> findByStatus(SubscriptionStatus subscriptionStatus);
}
