package com.mallang.mallang_backend.domain.plan.repository;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {


    Plan findByPeriodAndType(PlanPeriod period, SubscriptionType type);
}
