package com.mallang.mallang_backend.global.config.oauth;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.global.config.oauth.processor.OAuth2UserProcessor;
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
import java.util.List;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.UNSUPPORTED_OAUTH_PROVIDER;

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

    private final List<OAuth2UserProcessor> processors;
    private final MemberService memberService;
    private final TokenService tokenService;
    private final JwtService jwtService;

    /**
     * OAuth2 인증 성공시 호출되는 엔트리 포인트 메서드
     *
     * @param request        인증 요청 객체
     * @param response       인증 응답 객체
     * @param authentication 인증 정보 객체
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        LoginPlatform platform = extractLoginPlatform(authentication);
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        processOAuthLogin(response, platform, user);
    }

    /**
     * OAuth2 로그인 처리 주요 흐름을 담당하는 메서드
     *
     * @param response   응답 객체
     * @param platform   로그인 플랫폼 정보
     * @param user       OAuth2 인증 사용자 정보
     * @throws IOException 리다이렉트 시 예외
     */
    private void processOAuthLogin(HttpServletResponse response,
                                   LoginPlatform platform,
                                   OAuth2User user) throws IOException {
        Map<String, Object> userAttributes = parseUserAttributes(platform, user);
        String email = extractUniqueEmail(userAttributes);

        if (isExistingMember(email)) {
            handleExistingMember(response, email);
            redirectToMainPage(response);
            return;
        }

        registerNewMember(response, platform, userAttributes);
        redirectToAdditionalInfoPage(response);
    }

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

    /**
     * 기존 회원 처리: 토큰 생성 및 응답 설정 메서드
     *
     * @param response 응답 객체
     * @param email    회원 이메일(고유 ID)
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
     * 신규 회원 가입 처리 메서드
     *
     * @param response      응답 객체
     * @param platform      로그인 플랫폼 정보
     * @param attributes    OAuth2에서 추출한 사용자 속성 맵
     */
    private void registerNewMember(HttpServletResponse response,
                                   LoginPlatform platform,
                                   Map<String, Object> attributes) {
        String email = String.valueOf(attributes.get(ID_KEY));
        String nickname = (String) attributes.get(NICKNAME_KEY);
        String profileImage = (String) attributes.get(PROFILE_IMAGE_KEY);

        log.info("사용자 id: {}, nickname: {}, profileImage: {}", email, nickname, profileImage);

        Long memberId = memberService.signupByOauth(
                email, nickname, profileImage, platform
        );
        setJwtToken(response, memberId, memberService.getSubscription(memberId));
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

        // 2. 액세스 토큰 쿠키에 설정
        jwtService.setJwtSessionCookie(ACCESS_TOKEN, tokenPair.getAccessToken(), response);

        // 3. 리프레시 토큰 쿠키에 설정
        jwtService.setJwtSessionCookie(REFRESH_TOKEN, tokenPair.getRefreshToken(), response);
    }

    /**
     * 인증 객체에서 OAuth2 제공자 ID 추출 및 플랫폼 변환 메서드
     *
     * @param authentication Spring Security 인증 객체
     * @return 로그인 플랫폼 정보
     * @throws ServiceException 인증 타입이 잘못된 경우
     */
    private LoginPlatform extractLoginPlatform(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new ServiceException(UNSUPPORTED_OAUTH_PROVIDER);
        }

        String providerId = oauthToken.getAuthorizedClientRegistrationId();
        log.info("OAuth2 제공자 식별자: {}", providerId);

        return LoginPlatform.from(providerId.toLowerCase());
    }

    /**
     * 메인 페이지로 리다이렉트하는 메서드
     *
     * @param response 응답 객체
     * @throws IOException 리다이렉트 시 예외
     */
    private void redirectToMainPage(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontUrl);
    }

    /**
     * 추가 정보 입력 페이지로 리다이렉트하는 메서드
     *
     * @param response 응답 객체
     * @throws IOException 리다이렉트 시 예외
     */
    private void redirectToAdditionalInfoPage(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontUrl + "/additional_info");
    }

    /**
     * 회원 존재 여부 확인 메서드
     *
     * @param email 회원 이메일(고유 ID)
     * @return 회원 존재 여부
     */
    private boolean isExistingMember(String email) {
        return memberService.isExistEmail(email);
    }
}
