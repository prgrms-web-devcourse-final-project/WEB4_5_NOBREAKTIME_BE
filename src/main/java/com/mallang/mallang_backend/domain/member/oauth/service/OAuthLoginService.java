package com.mallang.mallang_backend.domain.member.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.oauth.dto.RejoinBlockResponse;
import com.mallang.mallang_backend.domain.member.dto.SignupRequest;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.oauth.processor.OAuth2UserProcessor;
import com.mallang.mallang_backend.domain.member.service.main.MemberService;
import com.mallang.mallang_backend.domain.member.service.withdrawn.MemberWithdrawalService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.exception.custom.LockAcquisitionException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 로그인 서비스 처리 (트랜잭션 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final MemberService memberService;
    private final List<OAuth2UserProcessor> processors;
    private final MemberWithdrawalService withdrawalService;
    private final S3ImageUploader imageUploader;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * OAuth2 로그인 프로세스를 처리하는 메서드
     *
     * @param platform 사용자 로그인 플랫폼 (GOOGLE, KAKAO 등)
     * @param user     OAuth2 인증 정보를 담은 사용자 객체
     * @return 처리 완료된 OAuth2 사용자 객체
     * @throws LockAcquisitionException 분산 락 획득 실패 시 발생
     * @throws ServiceException         비즈니스 로직 처리 중 오류 발생 시
     */
    @Retryable(
            value = LockAcquisitionException.class, // 재시도할 예외 지정
            backoff = @Backoff(delay = 1000)  // 1초(1000ms) 간격으로 재시도
    )
    public OAuth2User processLogin(LoginPlatform platform,
                                   OAuth2User user) {
        Map<String, Object> userAttributes = parseUserAttributes(platform, user);
        String platformId = user.getName();
        log.debug("획득한 platformId: {}", platformId);

        String lockKey = "LOCK:" + JOIN_MEMBER_KEY + platformId;
        String lockValue = UUID.randomUUID().toString(); // 고유 식별자

        try {
            acquireDistributedLock(lockKey, lockValue);
            processMemberRegistration(platform, platformId, userAttributes);
            return new DefaultOAuth2User(Collections.emptyList(), userAttributes, "platformId");
        } finally {
            releaseDistributedLockSafely(lockKey, lockValue);
        }
    }

    /**
     * 분산 락 획득 시도
     */
    private void acquireDistributedLock(String lockKey, String lockValue) {
        Boolean isLockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(3));

        log.debug("분산 락 획득 상태: {}", isLockAcquired);
        if (!Boolean.TRUE.equals(isLockAcquired)) {
            throw new LockAcquisitionException(LOCK_ACQUIRED_FAILED);
        }
    }

    /**
     * 멤버 등록 처리 로직
     */
    private void processMemberRegistration(LoginPlatform platform,
                                           String platformId,
                                           Map<String, Object> attributes
    ) {
        String storedValue = redisTemplate.opsForValue().get(JOIN_MEMBER_KEY + platformId);
        log.debug("storedValue: {}", storedValue);

        // redis 에 값이 존재하지 않을 때에만 검증한다
        if (!platformId.equals(storedValue)) {
            registerNewMember(platform, attributes);
            return;
        }

        log.debug("이미 존재하는 회원입니다.");
    }

    /**
     * 락 안전 해제 로직
     */
    private void releaseDistributedLockSafely(String lockKey, String lockValue) {
        try {
            log.debug("분산 락 해제 시도: {}", lockKey);
            redisTemplate.execute(
                    LOCK_RELEASE_SCRIPT,
                    Collections.singletonList(lockKey),
                    lockValue
            );
            log.debug("분산 락 해제 완료");
        } catch (Exception e) {
            log.warn("락 해제 실패: {}", e.getMessage(), e);
        }
    }

    // === 회원 정보 추출 === //

    /**
     * 플랫폼별 사용자 속성 파싱 메서드
     *
     * @param platform 로그인 플랫폼 정보
     * @param user     OAuth2 인증 사용자 정보
     * @return 사용자 속성 맵
     */
    private Map<String, Object> parseUserAttributes(LoginPlatform platform,
                                                    OAuth2User user) {
        log.debug("user attributes {}", user.getAttributes());
        OAuth2UserProcessor processor = findSupportedProcessor(platform);
        return processor.parseAttributes(user.getAttributes());
    }

    private OAuth2UserProcessor findSupportedProcessor(LoginPlatform platform) {

        return processors.stream()
                .filter(p -> p.supports(platform))
                .findFirst()
                .orElseThrow(() -> new ServiceException(UNSUPPORTED_OAUTH_PROVIDER));
    }

    // === 회원 가입 처리 === //
    /**
     * 신규 회원을 등록합니다.
     *
     * @param platform       로그인 플랫폼 정보
     * @param userAttributes 사용자 속성 정보 (플랫폼에서 전달)
     * @throws ServiceException 30일 이내 탈퇴 이력이 있을 경우 예외 발생
     */
    public void registerNewMember(LoginPlatform platform,
                                  Map<String, Object> userAttributes) {

        log.debug("[OAuth 회원가입 시작] 플랫폼: {}", platform);

        String platformId = (String) userAttributes.get(PLATFORM_ID_KEY);

        String nickname = generateUniqueNickname((String) userAttributes.get(NICKNAME_KEY)); // 닉네임 중복 방지 로직 적용
        String s3ProfileImageUrl = uploadProfileImage((String) userAttributes.get(PROFILE_IMAGE_KEY)); // 프로필 이미지 S3 업로드

        executeSignupProcess(
                platform,
                userAttributes,
                platformId, nickname,
                s3ProfileImageUrl
        );

        storeMemberKeyInRedis(platformId); // 플랫폼 키 저장
    }

    // 회원 가입 진행 로직
    private void executeSignupProcess(LoginPlatform platform,
                                      Map<String, Object> userAttributes,
                                      String platformId,
                                      String nickname,
                                      String s3ProfileImageUrl
    ) {
        memberService.signupByOauth(new SignupRequest(
                platformId,
                (String) userAttributes.get("email"),
                nickname,
                s3ProfileImageUrl,
                platform)
        );
    }

    // 회원 가입 성공 시 회원 키 저장 (이후 로그인 시 키로 정보 조회)
    private void storeMemberKeyInRedis(String platformId) {
        String redisKey = JOIN_MEMBER_KEY + platformId;
        if (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent(redisKey, platformId))) {
            log.warn("[Redis 키 충돌] 이미 존재하는 회원 키: {}", redisKey);
            throw new ServiceException(MEMBER_ALREADY_JOINED);
        }
        log.debug("회원 키 저장 성공 키: {}, 값: {}", JOIN_MEMBER_KEY, platformId);
    }

    /**
     * 프로필 이미지를 S3에 업로드하고 URL을 반환합니다.
     *
     * @param profileImageUrl 업로드할 이미지 URL
     * @return S3에 업로드된 이미지 URL
     */
    private String uploadProfileImage(String profileImageUrl) {
        ImageUploadRequest request = new ImageUploadRequest(profileImageUrl);
        return imageUploader.uploadImageURL(request);
    }

    /**
     * 원본 닉네임을 기반으로 고유한 닉네임을 생성합니다.
     * - 중복 시 랜덤 접미사(2~3자리)를 추가
     * - 최대 5회 시도 후 예외 발생
     *
     * @param originalNickname 사용자가 입력한 원본 닉네임
     * @return 사용 가능한 고유 닉네임
     * @throws ServiceException 유일한 닉네임 생성 실패 시
     */
    private String generateUniqueNickname(String originalNickname) {
        final int MAX_ATTEMPTS = 5;
        String currentNickname = originalNickname;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            if (!memberService.existsByNickname(currentNickname)) {
                return currentNickname;
            }
            String randomSuffix = RandomStringGenerator.generate(2 + new SecureRandom().nextInt(2));
            currentNickname = originalNickname + randomSuffix;
        }

        log.error("닉네임 생성 실패: {}", originalNickname);
        throw new ServiceException(NICKNAME_GENERATION_FAILED);
    }


    /**
     * 탈퇴 이력이 있는 회원의 재가입 가능 여부를 검증합니다.
     * 재가입 가능일 이전일 시 회원 정보를 삭제하고 예외를 발생시킵니다.
     *
     * @param platformId 플랫폼 식별자
     */
    private void validateWithdrawnLogNotRejoinable(String platformId) {
        // 탈퇴 이력이 없으면 바로 리턴
        if (!withdrawalService.existsByOriginalPlatformId(platformId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(); // 현재 시간 고정

        // 탈퇴 이력 조회
        Optional.ofNullable(withdrawalService.findByOriginalPlatformId(platformId))
                .map(WithdrawnLog::getCreatedAt)
                .map(dt -> dt.plusDays(30))
                .filter(date -> LocalDateTime.now().isBefore(date))
                // 재가입 가능일이 아직 지나지 않은 경우 예외 처리
                .ifPresent(date -> {
                    log.warn("[재가입 차단] platformId: {}", platformId);
                    RejoinBlockResponse response = sendErrorMessage(date, now);

                    throw new ServiceException(REJOIN_BLOCKED);
                });
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
}
