package com.mallang.mallang_backend.domain.plan.entity.domain.subscription.service;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.plan.entity.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.plan.entity.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.plan.entity.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.plan.entity.domain.subscription.entity.SubscriptionStatus;
import com.mallang.mallang_backend.domain.plan.entity.domain.subscription.repository.SubscriptionQueryRepository;
import com.mallang.mallang_backend.domain.plan.entity.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.mallang.mallang_backend.global.exception.ErrorCode.SUBSCRIPTION_NOT_FOUND;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionQueryRepository queryRepository;
    private final PaymentRepository paymentRepository;

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
        LocalDateTime expiredDate = startDate.plusMonths(plan.getPeriod().getMonths());

        Member member = findMemberOrThrow(memberId);
        SubscriptionType preType = member.getSubscriptionType();
        member.updateSubscription(plan.getType());

        Subscription newSubs = Subscription.builder()
                .member(member)
                .plan(plan)
                .startedAt(startDate)
                .expiredAt(expiredDate) // 계산된 시간 사용
                .build();

        subscriptionRepository.save(newSubs);

        log.info("[결제변경이력] 사용자 ID:{}|구독등급:{}→{}|변경기간:{}~{}",
                memberId, preType, member.getSubscriptionType(),
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

    // TODO 변경 필요 -> 자동으로 변경이 되고 결제가 되도록 스케줄링 할 것
    @Transactional
    public void updateSubscriptionStatus(Long memberId,
                                         Clock clock
    ) {
        Member member = findMemberOrThrow(memberId);

        // 최신 구독만 조회
        Optional<Subscription> optionalSubscription = queryRepository.findLatestByMember(member);
        optionalSubscription.ifPresent(subscription -> {
            // 만료 시간이 현재보다 이전일 때 상태 변경
            if (subscription.getExpiredAt().isBefore(LocalDateTime.now(clock))) {
                subscription.updateStatus(SubscriptionStatus.EXPIRED);
            }
        });
    }

    // 구독 갱신 해지 시, 빌링 키와 고객 키를 삭제 + 취소 상태로 변경
    // TODO 자독 구독 갱신 이벤트 -> 페이먼트 객체에서 빌링 키가 있는 회원을 한정으로 실행 혹은 AutoRenew 로 판단
    @Override
    @Transactional
    public void cancelSubscription(Long memberId) {
        Member member = findMemberOrThrow(memberId);

        // 최신 한 건 조회
        Subscription subscription = queryRepository.findLatestByMember(member).orElseThrow(
                () -> new ServiceException(SUBSCRIPTION_NOT_FOUND));

        subscription.updateStatus(SubscriptionStatus.CANCELED);
        subscription.updateAutoRenew(false); // 자동 결제 여부 초기화

        // TODO 구독 만료일을 조회하고, 만료일까지는 상태가 변경되어서는 안 됨 -> 이 또한 스케줄링으로 처리
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
