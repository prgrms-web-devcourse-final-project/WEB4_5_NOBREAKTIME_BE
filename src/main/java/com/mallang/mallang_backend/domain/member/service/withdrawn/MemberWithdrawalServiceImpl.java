package com.mallang.mallang_backend.domain.member.service.withdrawn;


import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLogRepository;
import com.mallang.mallang_backend.domain.member.query.MemberQueryRepository;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLTransientConnectionException;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberWithdrawalServiceImpl implements MemberWithdrawalService {

    private final MemberRepository memberRepository;
    private final SubscriptionService subscriptionService;
    private final WithdrawnLogRepository logRepository;
    private final MemberQueryRepository memberQueryRepository;

    /**
     * 회원 탈퇴 처리
     * - 활성 구독 존재 시 BASIC 등급으로 다운그레이드
     * - 회원 탈퇴 일자를 withdrawalDate 에 추가 후 개인정보 마스킹
     *
     * @param memberId 대상 회원 ID
     */
    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = findMemberOrThrow(memberId);
        String uuid = UUID.randomUUID().toString();

        saveWithdrawnMemberLog(memberId, member, uuid);

        member.markAsWithdrawn(uuid);

        log.debug("회원 탈퇴 로그 저장 완료: {}", member.getId());

        if (subscriptionService.hasActiveSubscription(memberId)) {
            subscriptionService.downgradeSubscriptionToBasic(memberId);
        }
    }

    // 탈퇴 회원 DB 저장 -> 추후 재가입 시 30일 이후 재가입 가능하도록
    private void saveWithdrawnMemberLog(Long memberId,
                                        Member member,
                                        String uuid) {

        logRepository.save(WithdrawnLog.builder()
                .memberId(memberId)
                .originalPlatformId(member.getPlatformId())
                .uuid(uuid)
                .build());
    }

    /**
     * 6개월 이상 경과한 탈퇴 회원 일괄 삭제
     * - 매일 새벽 3시 실행
     * - 일시적 오류 발생(DB 연결, 네트워크 오류) 시 최대 3회 재시도 (5초 간격)
     * - 비즈니스 로직 오류 (외래키 제약 조건 등): 재시도 의미 없어 실행하지 않음
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000),
            include = {TransientDataAccessException.class, SQLTransientConnectionException.class},
            exclude = {IllegalArgumentException.class, DataIntegrityViolationException.class}
    )
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void scheduleAccountDeletion() {
        LocalDateTime deletionThreshold = LocalDateTime.now().minusMonths(6);
        long deletedCount = memberQueryRepository.bulkDeleteExpiredMembers(deletionThreshold);

        log.info("탈퇴 완료 후 6개월 경과 회원 삭제 완료 - 삭제 건수: {}", deletedCount);
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

    @Override
    public boolean existsByOriginalPlatformId(String platformId) {
        return logRepository.existsByOriginalPlatformId(platformId);
    }

    @Override
    public WithdrawnLog findByOriginalPlatformId(String platformId) {
        return logRepository.findByOriginalPlatformId(platformId);
    }
}