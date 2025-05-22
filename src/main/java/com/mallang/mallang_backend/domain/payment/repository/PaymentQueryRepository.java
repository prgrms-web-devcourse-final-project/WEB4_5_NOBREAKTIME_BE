package com.mallang.mallang_backend.domain.payment.repository;

import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.entity.QPayment;
import com.mallang.mallang_backend.domain.plan.entity.QPlan;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class PaymentQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Optional<Payment> findLatestByMemberId(Long memberId) {

        QPayment payment = new QPayment("payment");
        QPlan plan = new QPlan("plan");

        return Optional.ofNullable(queryFactory.selectFrom(payment)
                .join(payment.plan, plan).fetchJoin() // Plan 즉시 로딩
                .where(payment.memberId.eq(memberId))
                .orderBy(payment.approvedAt.desc()) // 생성일 기준 내림차순
                .limit(1)
                .fetchOne());
    }
}