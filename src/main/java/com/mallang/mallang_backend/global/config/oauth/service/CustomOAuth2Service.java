package com.mallang.mallang_backend.global.config.oauth.service;

import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLogRepository;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.main.MemberService;
import com.mallang.mallang_backend.global.config.oauth.processor.OAuth2UserProcessor;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 성공 핸들러에서 사용자 정보를 받아 로그인 + DB 추가 과정을 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2Service extends DefaultOAuth2UserService {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final List<OAuth2UserProcessor> processors;
    private final S3ImageUploader imageUploader;
    private final WithdrawnLogRepository logRepository;

    @Retry(name = "apiRetry", fallbackMethod = "fallbackMethod")
    @CircuitBreaker(name = "oauthUserLoginService", fallbackMethod = "fallbackMethod")
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = super.loadUser(userRequest); // 외부 API 호출 발생 지점

        return processOAuthLogin(LoginPlatform.from(provider.toLowerCase()), oAuth2User);
    }

    /**
     * OAuth2 로그인 처리 주요 흐름을 담당하는 메서드
     *
     * @param platform 로그인 플랫폼 정보
     * @param user     OAuth2 인증 사용자 정보
     */
    private DefaultOAuth2User processOAuthLogin(LoginPlatform platform,
                                                OAuth2User user) {

        Map<String, Object> userAttributes = parseUserAttributes(platform, user);
        String platformId = user.getName();
        log.debug("platformId: {}", platformId);

        if (memberService.existsByPlatformId(platformId)) {
            handleExistingMember(platformId);
        } else {
            registerNewMember(platform, userAttributes);
        }

        return new DefaultOAuth2User(Collections.emptyList(), userAttributes, "platformId");
    }

    // ======================회원 정보 추출=======================

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

    // ======================회원 로그인 & 가입 처리=======================

    /**
     * 플랫폼 ID로 기존 회원의 존재 여부를 비동기적으로 확인합니다.
     * 추후 필요에 따라 후속 작업(예: 로그, 알림 등)을 수행할 수 있습니다.
     *
     * @param platformId 확인할 회원의 플랫폼 ID
     */
    @Async("securityTaskExecutor")
    public void handleExistingMember(String platformId) {

        Member member = memberRepository.findByPlatformId(platformId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        if (member.getPlatformId() == null) {
            throw new ServiceException(MEMBER_ALREADY_WITHDRAWN);
        }

        log.info("이미 존재하는 회원: {}", platformId);
    }

    // 추후 서비스 로직 분리 필요
    /**
     * 신규 회원을 등록합니다.
     *
     * @param platform       로그인 플랫폼 정보
     * @param userAttributes 사용자 속성 정보 (플랫폼에서 전달)
     * @throws ServiceException 30일 이내 탈퇴 이력이 있을 경우 예외 발생
     */
    @Async("securityTaskExecutor")
    public void registerNewMember(LoginPlatform platform,
                                  Map<String, Object> userAttributes) {

        // 필수 속성 추출
        String platformId = (String) userAttributes.get(PLATFORM_ID_KEY);

        // 30일 이내 탈퇴 이력이 존재하면 예외 발생
        validateWithdrawnLogNotRejoinable(platformId);

        String email = (String) userAttributes.get("email");
        String originalNickname = (String) userAttributes.get(NICKNAME_KEY);
        String profileImage = (String) userAttributes.get(PROFILE_IMAGE_KEY);

        // 닉네임 중복 방지 로직 적용
        String nickname = generateUniqueNickname(originalNickname);

        log.debug("사용자 platformId: {}, email: {}, nickname: {}, profileImage: {}",
                platformId, email, nickname, profileImage);

        // 프로필 이미지 S3 업로드
        String s3ProfileImageUrl = uploadProfileImage(profileImage);

        memberService.signupByOauth(
                platformId,
                email,
                originalNickname,
                s3ProfileImageUrl,
                platform
        );
    }

    /**
     * 30일 이내 탈퇴 이력이 있는 경우 가입을 제한합니다.
     *
     * @param platformId 플랫폼 고유 아이디
     * @throws ServiceException 30일 이내 탈퇴 이력이 있을 경우
     */
    private void validateWithdrawnLogNotRejoinable(String platformId) {

        if (logRepository.existsByOriginalPlatformId(platformId)) {

            WithdrawnLog withdrawnLog = logRepository.findByOriginalPlatformId(platformId);
            LocalDateTime rejoinAvailableAt = withdrawnLog.getCreatedAt().plusDays(30); // 가입 가능 날짜

            if (rejoinAvailableAt.isAfter(LocalDateTime.now())) {
                log.warn("아직 가입할 수 없는 회원: {}", platformId);
                throw new ServiceException(CANNOT_SIGNUP_WITH_THIS_ID);
            }
        }
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
            if (memberService.isNicknameAvailable(currentNickname)) {
                return currentNickname;
            }
            String randomSuffix = RandomStringGenerator.generate(2 + new SecureRandom().nextInt(2));
            currentNickname = originalNickname + randomSuffix;
        }

        log.error("닉네임 생성 실패: {}", originalNickname);
        throw new ServiceException(NICKNAME_GENERATION_FAILED);
    }

    private OAuth2User fallbackMethod(OAuth2UserRequest userRequest,
                                      Exception e) {

        if (e instanceof ResourceAccessException) {
            log.error("OAuth 서버 연결 실패: {}", e.getMessage());
            throw new ServiceException(OAUTH_NETWORK_ERROR, e);
        } else if (e instanceof HttpClientErrorException.TooManyRequests) {
            handleTooManyRequests((HttpClientErrorException) e);
        } else if (e instanceof OAuth2AuthenticationException) {
            handleOAuthException((OAuth2AuthenticationException) e);
        } else if (e instanceof CallNotPermittedException) {
            log.warn("서킷 브레이커 활성화 - 30초간 호출 차단");
            throw new ServiceException(API_BLOCK, e);
        }
        throw new ServiceException(API_ERROR, e);
    }

    private void handleTooManyRequests(HttpClientErrorException e) {
        HttpHeaders headers = e.getResponseHeaders();
        String retryAfter = headers != null ? headers.getFirst("Retry-After") : "60";
        log.warn("API 호출 제한 - 재시도까지 {}초 남음", retryAfter);
        throw new ServiceException(ErrorCode.OAUTH_RATE_LIMIT);
    }

    private void handleOAuthException(OAuth2AuthenticationException ex) {
        OAuth2Error error = ex.getError();
        if (error.getErrorCode().equals("invalid_token")) {
            throw new ServiceException(INVALID_TOKEN, ex);
        }
        throw new ServiceException(UNSUPPORTED_OAUTH_PROVIDER, ex);
    }
}
