package com.mallang.mallang_backend.global.token;

import com.mallang.mallang_backend.global.exception.ServiceException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

import static com.mallang.mallang_backend.global.constants.AppConstants.ACCESS_TOKEN;
import static com.mallang.mallang_backend.global.constants.AppConstants.REFRESH_TOKEN;
import static com.mallang.mallang_backend.global.exception.ErrorCode.TOKEN_EXPIRED;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refresh_expiration}")
    private Long refreshExpiration;

    // JWT 생성
    public String createToken(Map<String, Object> claims, Long expiration) {
        SecretKey key = getSecretKey(secretKey);

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256) // 알고리즘과 키를 명시적으로 지정
                .compact();
    }

    // JWT 검증 및 Claims 객체 추출
    public Claims parseAndValidateToken(String token) {
        SecretKey key = getSecretKey(secretKey);
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)  // verifyWith → setSigningKey 변경
                    .build()
                    .parseClaimsJws(token)  // parseSignedClaims → parseClaimsJws 변경
                    .getBody();  // getPayload() → getBody() 변경
        } catch (JwtException e) {
            handleAuthException(e);
            return null;
        }
    }

    //JWT -> 쿠키에 저장 (세션 쿠키)
    public void setJwtSessionCookie(String token,
                                    HttpServletResponse response) {

        Cookie cookie = new Cookie(ACCESS_TOKEN, token);
        cookie.setHttpOnly(true);      // 자바스크립트 접근 차단 (XSS 방지)
        cookie.setPath("/");           // 전체 사이트에서 접근 가능
        cookie.setSecure(true);        // HTTPS 통신 시에만 전송
        cookie.setAttribute("SameSite", "None"); // SameSite=None 속성 직접 추가

        response.addCookie(cookie);
    }

    // JWT -> 쿠키에 저장 (지속 쿠키)
    public void setJwtPersistentCookie(String token,
                                       HttpServletResponse response) {

        Cookie cookie = new Cookie(REFRESH_TOKEN, token);
        cookie.setPath("/");                // 전체 사이트에서 접근 가능
        cookie.setHttpOnly(true);
        cookie.setSecure(true);             // HTTPS 통신 시에만 전송
        cookie.setHttpOnly(true);           // 자바스크립트 접근 불가 (XSS 방지)

        // 밀리초(Long) → 초(int) 변환
        int maxAgeSeconds = (int) (refreshExpiration / 1000);
        cookie.setMaxAge(maxAgeSeconds);    // 쿠키 만료 시간(초 단위)

        // SameSite=Lax 속성 추가
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }

    // 쿠키에서 JWT 추출
    public Optional<String> getTokenByCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) { // 쿠키가 없을 경우
            return Optional.empty();
        }

        Arrays.stream(cookies)
                .forEach(cookie -> log.debug("Cookie Name: {}, Value: {}", cookie.getName(), cookie.getValue()));

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public SecretKey getSecretKey(String secretKey) {
        // 16진수 문자열 → 바이트 배열
        return Keys.hmacShaKeyFor(HexFormat.of().parseHex(secretKey));
    }

    // 예외 처리 중앙화
    private void handleAuthException(JwtException e) {
        if (e instanceof ExpiredJwtException) {
            throw new ServiceException(TOKEN_EXPIRED, e);
        } else {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}