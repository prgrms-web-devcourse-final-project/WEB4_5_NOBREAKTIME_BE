package com.mallang.mallang_backend.domain.subscription.repository;

import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
