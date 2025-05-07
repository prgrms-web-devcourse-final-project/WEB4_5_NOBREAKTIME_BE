package com.mallang.mallang_backend.global.token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mallang.mallang_backend.global.constants.AppConstants.REFRESH_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.access_expiration}")
    private Long access_expiration;

    @Value("${jwt.refresh_expiration}")
    private Long refresh_expiration;

    public static final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    // id, 권한 정보를 담은 토큰 생성 (액세스 토큰, 리프레시 토큰)
    public TokenPair createTokenPair(Long memberId, String roleName) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        claims.put("role", roleName);

        // 액세스 토큰 생성 (짧은 만료시간)
        String accessToken = jwtService.createToken(claims, access_expiration);

        // 리프레시 토큰 생성 (긴 만료시간)
        String refreshToken = jwtService.createToken(claims, refresh_expiration);

        return new TokenPair(accessToken, refreshToken);
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    public String createAccessToken(Long memberId, String roleName) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        claims.put("role", roleName);

        return jwtService.createToken(claims, access_expiration);
    }

    // 블랙리스트에서 토큰 확인
    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = blacklist.get(token);
        if (expirationTime == null) {
            return false;
        }
        // 만료 시간이 지난 경우 제거
        if (System.currentTimeMillis() > expirationTime) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /**
     * 로그아웃 시 해당 토큰을 쿠키에서 삭제하고, 블랙리스트에 추가합니다.
     *
     * @param response HttpServletResponse 객체
     * @param token    블랙리스트에 추가할 토큰 값 (refreshToken)
     */
    public void invalidateTokenAndBlacklistIfRefreshToken(HttpServletResponse response, String token) {
        if (response == null) return;

        deleteTokenInCookie(response);
        addToBlacklist(token);
    }

    /**
     * 쿠키에서 해당 토큰을 삭제합니다.
     *
     * @param response HttpServletResponse 객체
     */
    private void deleteTokenInCookie(HttpServletResponse response) {
        if (response == null) return;

        Cookie expiredCookie = new Cookie(REFRESH_TOKEN, null);
        expiredCookie.setMaxAge(0); // 쿠키 즉시 삭제
        expiredCookie.setPath("/"); // 전체 경로에 적용

        response.addCookie(expiredCookie);

        log.debug("토큰 쿠키가 삭제되었습니다. 쿠키 이름: {}, Value: {}", expiredCookie.getName(), expiredCookie.getValue());
    }

    /**
     * 블랙리스트에 토큰을 추가하는 메서드입니다.
     * 토큰과 만료 시간을 저장하여 이후 검증 시 사용합니다.
     *
     * @param token 블랙리스트에 추가할 토큰 문자열
     */
    public void addToBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) return;

        long expirationTime = System.currentTimeMillis() + refresh_expiration;
        blacklist.put(token, expirationTime);
        log.info("블랙리스트에 토큰 추가: {}, 만료 시간: {}", token, expirationTime);
    }
}