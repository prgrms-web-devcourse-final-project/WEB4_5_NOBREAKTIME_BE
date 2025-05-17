package com.mallang.mallang_backend.domain.plan.entity.domain.subscription.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.entity.QSubscription;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubscriptionQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 가장 마지막 구독 내역을 가져오기
    public Optional<Subscription> findLatestByMember(Member member) {
        QSubscription subscription = QSubscription.subscription;

        Subscription result = queryFactory
                .selectFrom(subscription)
                .where(subscription.member.eq(member))
                .orderBy(subscription.startedAt.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
