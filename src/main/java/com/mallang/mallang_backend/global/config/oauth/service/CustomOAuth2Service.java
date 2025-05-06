package com.mallang.mallang_backend.global.config.oauth.service;

import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.global.config.oauth.processor.OAuth2UserProcessor;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

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
    private final List<OAuth2UserProcessor> processors;
    private final S3ImageUploader imageUploader;

    // 실제 구현 예시: CustomCircuitBreakerConfig - [oauthUserLoginService] 성공 (702ms)
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
        String email = extractUniqueEmail(userAttributes);
        userAttributes.put("idToEmail", email);

        CustomUserDetails userDetails = null;
        if (memberService.isExistEmail(email)) {
            userDetails = handleExistingMember(email);
        } else {
            userDetails = registerNewMember(platform, userAttributes);
        }

        // @AuthenticationPrincipal OAuth2User principal 로 컨트롤러에서 이용 가능
        return new DefaultOAuth2User(userDetails.getAuthorities(), userAttributes, "idToEmail");
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

        OAuth2UserProcessor processor = findSupportedProcessor(platform);
        return processor.parseAttributes(user.getAttributes());
    }

    private OAuth2UserProcessor findSupportedProcessor(LoginPlatform platform) {

        return processors.stream()
                .filter(p -> p.supports(platform))
                .findFirst()
                .orElseThrow(() -> new ServiceException(UNSUPPORTED_OAUTH_PROVIDER));
    }

    /**
     * 회원 고유 ID(이메일로 사용) 추출 메서드
     *
     * @param userAttributes OAuth2에서 추출한 사용자 속성 맵
     * @return 회원 고유 ID
     */
    private String extractUniqueEmail(Map<String, Object> userAttributes) {
        return String.valueOf(userAttributes.get("id"));
    }

    // ======================회원 로그인 & 가입 처리=======================

    /**
     * 기존 회원 처리: 토큰 생성 및 응답 설정 메서드
     *
     * @param email 회원 이메일(고유 ID)
     */
    private CustomUserDetails handleExistingMember(String email) {
        // 이메일로 기존 회원 ID 조회, 회원의 구독 정보 추출
        Member member = memberService.getMemberByEmail(email);

        return new CustomUserDetails(member.getId(), member.getSubscription().getRoleName());
    }

    /**
     * 신규 회원 가입 처리 메서드
     *
     * @param platform   로그인 플랫폼 정보
     * @param attributes OAuth2에서 추출한 사용자 속성 맵
     */
    private CustomUserDetails registerNewMember(LoginPlatform platform,
                                   Map<String, Object> attributes) {

        String email = (String) attributes.get(ID_KEY);
        String nickname = (String) attributes.get(NICKNAME_KEY);
        String profileImage = (String) attributes.get(PROFILE_IMAGE_KEY);

        log.info("사용자 email: {}, nickname: {}, profileImage: {}", email, nickname, profileImage);

        // S3에 프로필 이미지 업로드
        ImageUploadRequest request = new ImageUploadRequest(profileImage);
        String s3ProfileImageUrl = imageUploader.uploadImageURL(request);

        Long memberId = memberService.signupByOauth(
                email, nickname, s3ProfileImageUrl, platform
        );

        Member joinMember = memberService.getMemberById(memberId);

        return new CustomUserDetails(joinMember.getId(), joinMember.getSubscription().getRoleName());
    }

    private void fallbackMethod(Exception e) {
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
    }

    private void handleTooManyRequests(HttpClientErrorException e) {
        HttpHeaders headers = e.getResponseHeaders();
        String retryAfter = headers != null ? headers.getFirst("Retry-After") : "60";
        log.warn("🔒 API 호출 제한 - 재시도까지 {}초 남음", retryAfter);
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
