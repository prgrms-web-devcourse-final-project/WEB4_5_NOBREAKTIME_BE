package com.mallang.mallang_backend.domain.plan.entity.domain.payment.history;

import com.mallang.mallang_backend.domain.plan.entity.domain.payment.entity.PayStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.mallang.mallang_backend.domain.payment.history.QPaymentHistory.*;

@Repository
@RequiredArgsConstructor
public class PaymentHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    // paymentId 로 PaymentHistory 에서 가장 최근의 상태를 가져옵니다.
    public PayStatus findByPaymentLastStatus(Long paymentId){
        return queryFactory.select(paymentHistory.status)
                .from(paymentHistory)
                .where(paymentHistory.payment.id.eq(paymentId))
                .orderBy(paymentHistory.changedAt.desc())
                .limit(1)
                .fetchOne();
    }
}
