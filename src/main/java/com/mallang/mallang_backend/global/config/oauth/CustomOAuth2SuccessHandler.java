package com.mallang.mallang_backend.global.config.oauth;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.service.main.MemberService;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenPair;
import com.mallang.mallang_backend.global.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 / 회원가입 후
 * 로그인 -> 메인 페이지 / 회원가입 -> 언어 선택 페이지로 리다이렉트 할 것
 * <p>
 * kakao: {URL}/oauth2/authorization/kakao 로 이동
 * naver: {URL}/oauth2/authorization/naver 로 이동
 * google: {URL}/oauth2/authorization/google 로 이동
 */

/**
 * 소셜 로그인 회원 -> 공통적으로 ID 값을 이메일로 추가
 * 네이버 예시: QRvQa5AeTZ3xK8bwGUTwNFmIxjEvQYCBmV1o9PP7s-s (영문 String)
 * 카카오 예시: 4233017369 (숫자 String)
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

    /**
     * OAuth2 인증 성공시 호출되는 엔트리 포인트 메서드
     *
     * @param request        인증 요청 객체
     * @param response       인증 응답 객체
     * @param authentication 인증 정보 객체
     *                       -> CustomOAuth2Service 에서 리턴한 DefaultOAuth2User 객체
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        String platformId = authentication.getName();
        Member member = memberService.getMemberByPlatformId(platformId);

        setJwtToken(response, member.getId(), member.getSubscriptionType().getRoleName());

        if (member.getLanguage() == Language.NONE) {
            response.sendRedirect(frontUrl + "/additional_info");
        } else {
            response.sendRedirect(frontUrl + "/dashboard");
        }
    }

    /**
     * jwt 토큰을 헤더, 쿠키에 저장하는 메서드
     *
     * @param response 응답에 저장하기 위한 파라미터
     * @param memberId member 고유 값
     * @param roleName 구독에서 가져온 구독별 권한 설정 값
     */
    private void setJwtToken(HttpServletResponse response,
                             Long memberId,
                             String roleName) {

        // 1. 토큰 생성
        TokenPair tokenPair = tokenService.createTokenPair(memberId, roleName);

        // 2. 액세스 토큰 쿠키에 설정
        jwtService.setJwtSessionCookie(tokenPair.getAccessToken(), response);
        log.info("소셜 로그인 사용자 액세스 토큰: {}", tokenPair.getAccessToken());

        // 3. 리프레시 토큰 쿠키에 설정
        jwtService.setJwtPersistentCookie(tokenPair.getRefreshToken(), response);
    }
}
