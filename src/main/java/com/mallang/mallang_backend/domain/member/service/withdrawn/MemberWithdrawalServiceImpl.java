package com.mallang.mallang_backend.domain.member.service.withdrawn;


import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLogRepository;
import com.mallang.mallang_backend.domain.member.oauth.dto.RejoinBlockResponse;
import com.mallang.mallang_backend.domain.member.query.MemberQueryRepository;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.sql.SQLTransientConnectionException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mallang.mallang_backend.global.constants.AppConstants.JOIN_MEMBER_KEY;
import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberWithdrawalServiceImpl implements MemberWithdrawalService {

    @Value("${custom.site.frontUrl}")
    private String frontUrl;

    private final MemberRepository memberRepository;
    private final SubscriptionService subscriptionService;
    private final WithdrawnLogRepository logRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final RedisTemplate<String, String> redisTemplate;

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
        redisTemplate.delete(JOIN_MEMBER_KEY + member.getPlatformId());

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

    @Transactional
    @Override
    public boolean handleRejoinScenario(HttpServletResponse response, String platformId) {
        boolean isRedirected = validateWithdrawnLogNotRejoinable(response, platformId);
        if (isRedirected) {
            memberRepository.deleteByPlatformId(platformId);
            redisTemplate.delete(JOIN_MEMBER_KEY + platformId);
            return true;
        }
        return false;
    }

    /**
     * 탈퇴 이력이 있는 회원의 재가입 가능 여부를 검증합니다.
     * 재가입 가능일 이전일 시 회원 정보를 삭제하고 예외를 발생시킵니다.
     *
     * @param platformId 플랫폼 식별자
     */
    private boolean validateWithdrawnLogNotRejoinable(HttpServletResponse response,
                                                      String platformId
    ) {
        // 탈퇴 이력이 없으면 바로 리턴
        if (!existsByOriginalPlatformId(platformId)) {
            return false;
        }

        AtomicBoolean isRedirected = new AtomicBoolean(false); // 상태 추적
        LocalDateTime now = LocalDateTime.now(); // 현재 시간 고정

        // 탈퇴 이력 조회
        Optional.ofNullable(findByOriginalPlatformId(platformId))
                .map(WithdrawnLog::getCreatedAt)
                .map(dt -> dt.plusDays(30))
                .filter(date -> LocalDateTime.now().isBefore(date))
                // 재가입 가능일이 아직 지나지 않은 경우 예외 처리
                .ifPresent(date -> {
                    log.warn("[재가입 차단] platformId: {}", platformId);
                    RejoinBlockResponse rsp = sendErrorMessage(date, now);
                    String redirectUrl = UriComponentsBuilder.fromUriString(frontUrl)
                            .path("/login")
                            .queryParam("availableDate", rsp.getAvailableDate())
                            .queryParam("daysLeft", rsp.getDaysLeft())
                            .build()
                            .encode()
                            .toUriString();
                    try {
                        response.sendRedirect(redirectUrl);
                        isRedirected.set(true); // 성공 시 플래그 변경
                    } catch (IOException e) {
                        throw new RuntimeException("리다이렉트 실패", e);
                    }
                });

        return isRedirected.get(); // 상태 반환
    }

    private RejoinBlockResponse sendErrorMessage(LocalDateTime date, LocalDateTime now) {
        // 1. 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String formattedDate = date.format(formatter);

        // 2. 남은 시간 계산
        Duration duration = Duration.between(now, date);
        long days = duration.toDays();

        // 3. 메시지 생성
        return new RejoinBlockResponse(
                409,
                formattedDate,
                days
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

    @Override
    public boolean existsByOriginalPlatformId(String platformId) {
        return logRepository.existsByOriginalPlatformId(platformId);
    }

    @Override
    public WithdrawnLog findByOriginalPlatformId(String platformId) {
        return logRepository.findByOriginalPlatformId(platformId);
    }
}