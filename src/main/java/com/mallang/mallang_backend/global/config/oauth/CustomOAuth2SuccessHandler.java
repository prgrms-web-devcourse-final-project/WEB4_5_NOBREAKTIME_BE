package com.mallang.mallang_backend.global.config.oauth;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.service.MemberService;
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

/**
 * 로그인 / 회원가입 후
 * 로그인 -> 대시보드 페이지 / 회원가입 -> 언어 선택 페이지로 리다이렉트 할 것
 *
 * 프론트: http://localhost:8080/oauth2/authorization/kakao 로 이동
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
        Authentication authentication
    ) throws IOException {

        // 사용자의 정보를 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 정보 제공자 판별 용도 -> 로그인 플랫폼 저장 용도
        LoginPlatform loginPlatform = LoginPlatform.from(extractProvider(authentication));

        // id 값 추출 (이메일로 사용)
        String email = String.valueOf(attributes.get("id"));

        // 이미 존재하는 회원이라면 로그인 처리
        if (memberService.isExistEmail(email)) {
            setJwtToken(response, email, memberService.getMemberId(email));
            response.sendRedirect(frontUrl + "/dashboard"); // 로그인 후 대시보드로 리다이렉트 처리
        } else {
            // properties 내부 정보 추출
            Map<String, Object> properties = getProperties(attributes);
            String nickname = (String) properties.get("nickname");
            String profileImage = (String) properties.get("profile_image");
            log.info("사용자 id: {}, nickname: {}, profileImage: {}", email, nickname, profileImage);

            // 새롭게 회원 가입 처리
            Long memberId = memberService.signupByOauth(email, nickname, profileImage, loginPlatform);
            setJwtToken(response, email, memberId);
            response.sendRedirect(frontUrl + "/additional_info"); // 언어 선택 창으로 이동
        }
    }

    // 응답 헤더, 쿠키에 jwt 토큰 설정
    private void setJwtToken(
        HttpServletResponse response,
        String email,
        Long memberId
    ) {
        TokenPair tokenPair = tokenService.createTokenPair(email, memberId);
        response.setHeader("Authorization", "Bearer " + tokenPair.getAccessToken());
        jwtService.setJwtSessionCookie(tokenPair.getRefreshToken(), response);
    }

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
            throw new IllegalArgumentException("attributes 가 Map 타입으로 변환되지 않습니다.");
        }
    }

    private String extractProvider(Authentication authentication) {
        String registrationId = null;
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            registrationId = oauthToken.getAuthorizedClientRegistrationId();
            log.info("registrationId: {}", registrationId); // registrationId: kakao
        }
        return registrationId;
    }
}
