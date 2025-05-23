package com.mallang.mallang_backend.domain.payment.repository;

import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.entity.QPayment;
import com.mallang.mallang_backend.domain.plan.entity.QPlan;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.mallang.mallang_backend.domain.payment.entity.QPayment.payment;
import static com.mallang.mallang_backend.domain.payment.service.process.complete.CompletePayServiceImpl.MemberGrantedInfo;
import static com.mallang.mallang_backend.domain.plan.entity.QPlan.plan;


@Repository
@Transactional(readOnly = true)
public class PaymentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public PaymentQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

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

    /**
     * 주문 ID(orderId)를 기반으로 멤버의 권한 정보를 조회합니다.
     *
     * @param orderId 조회할 주문 ID
     * @return MemberGrantedInfo 멤버 권한 정보
     */
    public MemberGrantedInfo findMemberGrantedInfoWithRole(String orderId) {
        return queryFactory
                .select(Projections.constructor(
                        MemberGrantedInfo.class,
                        payment.memberId,
                        payment.plan.type
                ))
                .from(payment)
                .innerJoin(payment.plan, plan)
                .where(payment.orderId.eq(orderId))
                .fetchOne();
    }
}