package com.mallang.mallang_backend.domain.subscription.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.entity.SubscriptionStatus;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionQueryRepository;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionQueryRepository queryRepository;

    /**
     * member 에 접근해서 구독 정보를 가져 오기
     *
     * @param memberId
     * @return member 의 구독 타입에서 가져온 권한 정보
     */
    @Override
    public String getRoleName(Long memberId) {
        return findMemberOrThrow(memberId).getSubscriptionType().getRoleName();
    }

    /**
     * 사용자가 활성 구독 상태인지 확인
     *
     * @param memberId 회원 ID
     * @return 활성 구독 상태면 true, 아니면 false
     */
    public boolean hasActiveSubscription(Long memberId) {
        final Member member = findMemberOrThrow(memberId);
        return member.getSubscriptionType() != SubscriptionType.BASIC;
    }

    /*public boolean hasActiveSubscription(Long memberId) {
        Member member = findMemberOrThrow(memberId);
        List<Subscription> subscriptions = subscriptionRepository.findByMember(member);
        Subscription last = subscriptions.getLast();
        if (last.getStatus() == SubscriptionStatus.ACTIVE) {
            return true;
        } else {
            return false;
        }
    }*/

    // 구독 테이블 업데이트
    @Override
    public void updateSubscriptionInfo(Long memberId,
                                       Plan plan,
                                       Clock clock) {

        // 현재 시간을 Clock 기반으로 생성
        LocalDateTime startDate = LocalDateTime.now(clock);

        Member member = findMemberOrThrow(memberId);
        SubscriptionType type = member.getSubscriptionType();

        member.updateSubTypeAndLanguage(plan.getType());

        Subscription newSubs = Subscription.builder()
                .member(member)
                .plan(plan)
                .startedAt(startDate)
                .build();

        subscriptionRepository.save(newSubs);

        log.info("[결제변경이력] 사용자 ID:{}|구독등급:{}→{}|변경기간:{}~{}",
                memberId, type, member.getSubscriptionType(),
                newSubs.getStartedAt(), newSubs.getExpiredAt());
    }

    @Override
    public void updateIsAutoRenew(Long memberId) {
        // 1. 멤버 조회
        Member member = findMemberOrThrow(memberId);

        // 2. 최근 구독 이력 조회
        List<Subscription> subscriptions = subscriptionRepository.findByMember(member).get();
        if (subscriptions.isEmpty()) {
            log.error("[자동결제변경실패] 사용자ID:{} | 구독 이력이 존재하지 않습니다.", memberId);
            throw new ServiceException(SUBSCRIPTION_NOT_FOUND);
        }
        Subscription latestSubscription = subscriptions.getLast();

        // 3. 변경 전 값 저장
        boolean beforeAutoRenew = latestSubscription.getIsAutoRenew();

        // 4. 자동결제 여부 업데이트
        latestSubscription.updateAutoRenew(true);

        // 5. 변경 이력 로그 기록
        log.info("[결제변경이력] 사용자ID:{} | 자동결제여부:{}→{}",
                memberId, beforeAutoRenew, true);
    }

    // 구독 만료 설정 -> 이미 다운그레이드된 구독을 또 다운그레이드해도 문제없어야 함
    @Override
    public void downgradeSubscriptionToBasic(Long memberId) {
        Member member = findMemberOrThrow(memberId);

        // 변경 전 구독 등급 저장
        SubscriptionType beforeType = member.getSubscriptionType();

        // 구독 등급 BASIC 으로 변경
        member.updateSubscription(SubscriptionType.BASIC);

        // 구독 이력 조회 및 만료 처리
        List<Subscription> subscriptions = subscriptionRepository.findByMember(member).get();
        if (subscriptions == null || subscriptions.isEmpty()) {
            log.warn("[구독만료실패] 사용자ID:{} | 구독 이력이 존재하지 않습니다.", memberId);
            return;
        }

        Subscription lastSubscription = subscriptions.getLast();
        lastSubscription.updateStatus(SubscriptionStatus.EXPIRED); // 구독 만료 설정

        // 변경 이력 로그 기록 (변경 전/후 등급 모두 기록)
        log.info("[결제변경이력] 사용자 ID:{} | 구독등급:{}→{}",
                memberId, beforeType, SubscriptionType.BASIC);
    }

    @Override
    @Transactional
    @Retry(name = "dataSaveInstance", fallbackMethod = "updateSubscriptionStatusFallback")
    public void updateSubscriptionStatus() {
        log.info("[구독만료조회] 구독만료 조회 시작 (ID 값만) | @Async: {} | @Scheduled: {}",
                Thread.currentThread().getName(), LocalDateTime.now());
        // 최신 ACTIVE 구독, 어제까지의 만료일자를 가진 구독만 조회
        List<Long> activeSubscriptionIds = queryRepository.findActiveSubWithMember();

        if (!activeSubscriptionIds.isEmpty()) {
            long updatedCount = queryRepository.bulkUpdateStatus(activeSubscriptionIds);
            log.info("[구독만료성공] {}건 처리 완료", updatedCount);
            return;
        }

        log.info("[구독만료] 만료할 구독이 없습니다.");
    }

    public void updateSubscriptionStatusFallback(Exception e) {
        // 1. 에러 로깅
        log.error("[구독만료실패] {}", e.getMessage(), e);

        // 2. 알람 시스템 연동

        // 3. 예외를 다시 던져 스케줄러/모니터링 시스템이 인지하도록
        throw new ServiceException(SUBSCRIPTION_STATUS_UPDATE_FAILED, e);
    }

    // TODO 다시 가져올 것
    @Override
    public void cancelSubscription(Long memberId) {
        Member member = findMemberOrThrow(memberId);

        member.updateSubscription(SubscriptionType.BASIC);
    }

    /**
     * 회원을 조회하고, 없으면 예외를 발생
     *
     * @param memberId 회원 ID
     * @return 회원 엔티티
     */
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));
    }
}
