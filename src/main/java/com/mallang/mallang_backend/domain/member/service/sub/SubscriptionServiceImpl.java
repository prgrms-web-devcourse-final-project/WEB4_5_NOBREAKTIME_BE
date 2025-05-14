package com.mallang.mallang_backend.domain.member.service.sub;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;

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

    /**
     * 회원의 구독 상태를 변경
     *
     * @param memberId     회원 ID
     * @param subscriptionType 변경할 구독 상태
     */
    public void updateSubscriptionType(Long memberId, SubscriptionType subscriptionType) {
        Member member = findMemberOrThrow(memberId);
        member.updateSubscription(subscriptionType);
    }

    // 구독 테이블 업데이트
    @Override
    public void updateSubscriptionInfo(Long memberId,
                                       Plan plan,
                                       LocalDateTime startDate) {
        Member member = findMemberOrThrow(memberId);
        SubscriptionType preType = member.getSubscriptionType();
        member.updateSubscription(plan.getType());

        Subscription newSubs = Subscription.builder()
                .member(member)
                .plan(plan)
                .startedAt(startDate)
                .expiredAt(startDate.plusMonths(plan.getPeriod().getMonths()))
                .build();

        subscriptionRepository.save(newSubs);

        log.info("[결제변경이력] 사용자ID:{}|구독등급:{}→{}|변경기간:{}~{}",
                memberId,
                preType,
                member.getSubscriptionType(),
                newSubs.getStartedAt(),
                newSubs.getExpiredAt()
        );
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
