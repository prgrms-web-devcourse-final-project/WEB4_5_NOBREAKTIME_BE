package com.mallang.mallang_backend.domain.subscription.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.QMember;
import com.mallang.mallang_backend.domain.payment.quartz.dto.SubscriptionRenewalDto;
import com.mallang.mallang_backend.domain.plan.entity.QPlan;
import com.mallang.mallang_backend.domain.subscription.entity.QSubscription;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.entity.SubscriptionStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.mallang.mallang_backend.domain.subscription.entity.QSubscription.subscription;
import static com.mallang.mallang_backend.domain.subscription.entity.SubscriptionStatus.ACTIVE;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 가장 마지막 구독 내역을 가져오기
    public Optional<Subscription> findLatestByMember(Member member) {
        QSubscription subscription = QSubscription.subscription;

        Subscription result = queryFactory
                .selectFrom(subscription)
                .where(subscription.member.eq(member)) // 해당 회원의 구독 내역만 가져옴
                .where(subscription.status.eq(ACTIVE)) // ACTIVE 상태만 가져옴
                .orderBy(subscription.expiredAt.desc()) // 가장 만료일이 늦은(=가장 최근까지 유효한) 구독
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(result);
    }

    // 전체 구독 중 Member 별로 ACTIVE 상태만 가져오는 쿼리 (ID 값만)
    public List<Long> findActiveSubWithMember() {
        LocalDateTime todayStart =
                LocalDate.now(Clock.systemDefaultZone()).atStartOfDay(); // 오늘의 0시(자정)

        // expiredAt이 오늘 0시보다 이전인지(=어제까지 만료된 것만)
        // 오늘이 5월 17일이면, expiredAt이 5월 16일 23:59:59까지인 데이터만 조회
        return queryFactory
                .select(subscription.id)
                .from(subscription)
                .where(subscription.status.eq(ACTIVE)) // ACTIVE 상태만 가져옴
                .where(subscription.expiredAt.lt(todayStart))
                .orderBy(subscription.expiredAt.desc())
                .fetch();
    }

    public long bulkUpdateStatus(List<Long> ids) {
        return queryFactory
                .update(subscription)
                .set(subscription.status, SubscriptionStatus.EXPIRED)
                .where(subscription.id.in(ids))
                .execute(); // 실제 업데이트된 건수 반환
    }

    /**
     * 구독 자동 갱신 = 하루 전 날 구독 갱신 시도, 실패 시 당일 재시도, 이후 만료
     * expiredAt=2025-05-16T20:10 → 2025-05-15T00:00 에 갱신 처리가 필요
     * 1. 15일 자정에 expiredAt이 다음 날인 구독 선택 (5/16)
     * 2. 성공 시 새로운 구독 내역을 생성, 5/16~6/15 일까지 이용 가능한 구독이 추가 생성
     */
    @Transactional(readOnly = true)
    public List<SubscriptionRenewalDto> findAutoRenewable() {
        log.debug("자동 갱신 예약 처리 시작");
        LocalDateTime tomorrowStart = LocalDateTime.now()
                .plusDays(1)
                .truncatedTo(ChronoUnit.DAYS); // 2025-05-16T00:00

        LocalDateTime dayAfterTomorrowStart = tomorrowStart.plusDays(1); // 2025-05-17T00:00

        QMember member = new QMember("member");
        QPlan plan = new QPlan("plan");

        return queryFactory.select(Projections.constructor(
                        SubscriptionRenewalDto.class,
                        member.id,
                        plan.description
                ))
                .from(subscription)
                .where(subscription.isAutoRenew.eq(true),
                        subscription.status.eq(ACTIVE),
                        // expiredAt이 5월 16일 전체인 구독을 선택
                        subscription.expiredAt.between(tomorrowStart, dayAfterTomorrowStart))
                .fetch();
    }
}
