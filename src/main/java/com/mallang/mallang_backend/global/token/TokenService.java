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

    // 로그아웃 시 쿠키에 있는 토큰 삭제 및 블랙 리스트에 저장
    public void deleteTokens(HttpServletResponse response, String tokenName, String token) {
        if (tokenName.equals(REFRESH_TOKEN)) {
            addToBlacklist(token);
            deleteTokenInCookie(response);
        }
    }

    private void deleteTokenInCookie(HttpServletResponse response) {
        Cookie newToken = new Cookie(REFRESH_TOKEN, null); // 새 쿠키 생성
        newToken.setMaxAge(0); // 쿠키 즉시 삭제
        newToken.setPath("/");
        response.addCookie(newToken); // 클라이언트에 새 쿠키 전송 (기존 "token" 쿠키 덮어씌움)
        log.info("토큰 쿠키가 삭제되었습니다. 쿠키 이름: {}, Value: {}", newToken.getName(), newToken.getValue());
    }

    // 블랙리스트에 토큰 추가
    public void addToBlacklist(String token) {
        long expirationTime = System.currentTimeMillis() + refresh_expiration;
        blacklist.put(token, expirationTime);
        log.info("블랙리스트에 토큰 추가: {}, 만료 시간: {}", token, expirationTime);
    }
}