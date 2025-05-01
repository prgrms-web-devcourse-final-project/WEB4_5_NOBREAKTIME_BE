package com.mallang.mallang_backend.global.config.oauth;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenPair;
import com.mallang.mallang_backend.global.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.mallang.mallang_backend.global.exception.ErrorCode.INVALID_ATTRIBUTE_MAP;

/**
 * 로그인 / 회원가입 후
 * 로그인 -> 메인 페이지 / 회원가입 -> 언어 선택 페이지로 리다이렉트 할 것
 * <p>
 * 프론트: {URL}/oauth2/authorization/kakao 로 이동
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${custom.site.frontUrl}")
    private String frontUrl;

    private final MemberService memberService;
    private final TokenService tokenService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1. 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 로그인 플랫폼 추출
        LoginPlatform loginPlatform = LoginPlatform.from(extractProvider(authentication));

        // 3. 이메일(id) 추출
        String email = String.valueOf(attributes.get("id"));

        // 4. 회원 존재 여부 확인
        if (memberService.isExistEmail(email)) {
            handleExistingMember(response, email);
            response.sendRedirect(frontUrl + "/"); // 메인 페이지로 이동
            return;
        }

        // 5. 신규 회원 가입 처리
        Map<String, Object> properties = getProperties(attributes);
        String nickname = (String) properties.get("nickname");
        String profileImage = (String) properties.get("profile_image");
        log.info("사용자 id: {}, nickname: {}, profileImage: {}", email, nickname, profileImage);

        Long memberId = memberService.signupByOauth(email, nickname, profileImage, loginPlatform);
        setJwtToken(response, memberId, memberService.getSubscription(memberId));
        response.sendRedirect(frontUrl + "/additional_info"); // 언어 선택 창으로 이동
    }

    /**
     * 기존 사용자는 추가 저장 없이 바로 로그인 시키기 위한 메서드
     *
     * @param response 응답에 토큰 세팅하기 위한 파라미터
     * @param email    로그인 시 사용하는 id
     */
    private void handleExistingMember(HttpServletResponse response, String email) {
        // 1. 이메일로 기존 회원 ID 조회
        Long existMemberId = memberService.getMemberByEmail(email);

        // 2. 회원의 구독 정보 추출
        String subscription = memberService.getSubscription(existMemberId);

        // 3. JWT 토큰 생성 및 응답 설정
        setJwtToken(response, existMemberId, subscription);
    }

    /**
     * jwt 토큰을 헤더, 쿠키에 저장하는 메서드
     *
     * @param response 응답에 저장하기 위한 파라미터
     * @param memberId member 고유 값
     * @param roleName 구독에서 가져온 구독별 권한 설정 값
     */
    private void setJwtToken(HttpServletResponse response, Long memberId, String roleName) {
        // 1. 토큰 생성
        TokenPair tokenPair = tokenService.createTokenPair(memberId, roleName);

        // 2. 액세스 토큰 헤더에 설정
        response.setHeader("Authorization", "Bearer " + tokenPair.getAccessToken());

        // 3. 리프레시 토큰 쿠키에 설정
        jwtService.setJwtSessionCookie(tokenPair.getRefreshToken(), response);
    }

    /**
     * 소셜 로그인 후 넘어온 Properties 값에서 유효한 값을 꺼내오기 위한 메서드
     *
     * @param attributes
     * @return nickname, profile image url 을 반환
     */
    private Map<String, Object> getProperties(Map<String, Object> attributes) {
        Object propObj = attributes.get("properties");
        Map<String, Object> properties = null;
        if (propObj instanceof Map<?, ?> map) {
            // Map<?, ?>를 Map<String, Object>로 변환
            properties = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    properties.put(key, entry.getValue());
                }
            }
            return properties;
        } else {
            throw new ServiceException(INVALID_ATTRIBUTE_MAP);
        }
    }

    /**
     * 소셜 로그인 제공자를 찾기 위한 메서드
     *
     * @param authentication 시큐리티에서 넘어온 인증 객체
     * @return 예: kakao / google / naver
     */
    private String extractProvider(Authentication authentication) {
        String registrationId = null;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            registrationId = oauthToken.getAuthorizedClientRegistrationId();
            log.info("registrationId: {}", registrationId); // registrationId: kakao
        }

        return registrationId;
    }
}
