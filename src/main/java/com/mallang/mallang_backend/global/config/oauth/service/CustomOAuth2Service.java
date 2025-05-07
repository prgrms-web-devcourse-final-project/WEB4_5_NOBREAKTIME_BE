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
        String platformId = user.getName();

        CustomUserDetails userDetails = null;
        if (memberService.existsByPlatformId(platformId)) {
            userDetails = handleExistingMember(platformId);
        } else {
            userDetails = registerNewMember(platform, userAttributes);
        }

        // @AuthenticationPrincipal OAuth2User principal 로 컨트롤러에서 이용 가능
        return new DefaultOAuth2User(userDetails.getAuthorities(), userAttributes, "platformId");
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

    // ======================회원 로그인 & 가입 처리=======================

    /**
     * 플랫폼 ID로 기존 회원을 조회하여 CustomUserDetails 객체를 생성합니다.
     *
     * <p>회원의 ID와 구독 정보(roleName)를 조합하여 반환합니다.
     * 회원이 존재하지 않을 경우 예외는 상위 메서드에서 처리합니다.
     *
     * @param platformId 조회할 회원의 플랫폼 ID
     * @return 회원의 ID와 구독 정보(roleName: ROLE_BASIC)를 포함한 CustomUserDetails 객체
     */
    private CustomUserDetails handleExistingMember(String platformId) {
        Member member = memberService.getMemberByPlatformId(platformId);
        return new CustomUserDetails(member.getId(), member.getSubscription().getRoleName());
    }

    /**
     * 신규 회원 가입 처리 메서드
     *
     * @param platform   로그인 플랫폼 정보
     */
    private CustomUserDetails registerNewMember(LoginPlatform platform,
                                   Map<String, Object> userAttributes) {

        String platformId = (String) userAttributes.get(PLATFORM_ID_KEY);
        String email = (String) userAttributes.get("email"); // null 가능
        String nickname = (String) userAttributes.get(NICKNAME_KEY);
        String profileImage = (String) userAttributes.get(PROFILE_IMAGE_KEY);

        log.debug("사용자 platformId: {}, email: {}, nickname: {}, profileImage: {}",
                platformId, email, nickname, profileImage);

        // S3에 프로필 이미지 업로드
        ImageUploadRequest request = new ImageUploadRequest(profileImage);
        String s3ProfileImageUrl = imageUploader.uploadImageURL(request);

        Long memberId = memberService.signupByOauth(
                platformId, email, nickname, s3ProfileImageUrl, platform
        );

        Member joinMember = memberService.getMemberById(memberId);

        return new CustomUserDetails(joinMember.getId(), joinMember.getSubscription().getRoleName());
    }

    private OAuth2User fallbackMethod(OAuth2UserRequest userRequest, Exception e) {
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
