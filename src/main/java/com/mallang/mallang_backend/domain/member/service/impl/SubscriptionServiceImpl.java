package com.mallang.mallang_backend.domain.member.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.Subscription;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final MemberRepository memberRepository;

    /**
     * 사용자가 활성 구독 상태인지 확인
     *
     * @param memberId 회원 ID
     * @return 활성 구독 상태면 true, 아니면 false
     */
    public boolean hasActiveSubscription(Long memberId) {
        final Member member = findMemberOrThrow(memberId);
        return member.getSubscription() != Subscription.BASIC;
    }

    /**
     * 회원의 구독 상태를 변경
     *
     * @param memberId     회원 ID
     * @param subscription 변경할 구독 상태
     */
    @Transactional
    public void updateSubscription(Long memberId, Subscription subscription) {
        Member member = findMemberOrThrow(memberId);
        member.updateSubscription(subscription);
    }

    /**
     * 회원을 조회하고, 없으면 예외를 발생
     *
     * @param memberId 회원 ID
     * @return 회원 엔티티
     */
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
    }
}
