package com.mallang.mallang_backend.domain.subscription;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.global.common.Language;

import java.time.LocalDateTime;
import java.util.UUID;

public class EntityTestFactory {

    public static Member createMember() {
        String uuid = UUID.randomUUID().toString();
        return Member.builder()
                .email(uuid + "@test.com")
                .language(Language.ENGLISH)
                .loginPlatform(LoginPlatform.KAKAO)
                .nickname(uuid)
                .platformId(uuid)
                .profileImageUrl("")
                .build();
    }

    public static Plan createPlan(SubscriptionType type, PlanPeriod period) {
        int amount = type.getBasePrice();
        double discount = period.getDiscountRate();
        int months = period.getMonths();

        int total = (int) ((int) amount * months * discount);
        return Plan.builder()
                .period(period)
                .type(type)
                .amount(total)
                .benefits("")
                .build();
    }

    /**
     * 기본이 ACTIVE -> EXPIRED 가 필요하다면 변경 필요
     */
    public static Subscription createSubscription(Member member,
                                                  Plan plan,
                                                  LocalDateTime expiredAt) {

        return Subscription.builder()
                .plan(plan)
                .expiredAt(expiredAt)
                .startedAt(expiredAt.minusMonths(1))
                .member(member)
                .build();
    }
}
