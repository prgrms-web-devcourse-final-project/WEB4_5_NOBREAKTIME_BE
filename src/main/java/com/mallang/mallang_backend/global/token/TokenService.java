package com.mallang.mallang_backend.global.token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.mallang.mallang_backend.global.constants.AppConstants.REFRESH_TOKEN;
import static com.mallang.mallang_backend.global.constants.AppConstants.REFRESH_TOKEN_PREFIX;

/**
 * JWT 토큰 생성 및 Redis 저장을 담당하는 서비스입니다.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    // 만료 시간은 설정 파일에서 주입받거나 상수로 관리
    @Value("${jwt.access_expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh_expiration}")
    private Long refreshExpiration;

    public static final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 회원 ID와 권한 정보를 기반으로 액세스 토큰과 리프레시 토큰을 생성합니다.
     * 리프레시 토큰은 해시 처리 후 Redis 에 저장합니다.
     *
     * @param memberId 회원 식별자
     * @param roleName 권한 이름
     * @return 생성된 토큰 쌍(TokenPair)
     */
    public TokenPair createTokenPair(Long memberId, String roleName) {
        Map<String, Object> claims = createClaims(memberId, roleName);

        String accessToken = jwtService.createToken(claims, accessExpiration);
        String refreshToken = jwtService.createToken(claims, refreshExpiration);

        storeRefreshTokenInRedis(refreshToken, memberId);

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * JWT 클레임 정보를 생성합니다.
     *
     * @param memberId 회원 식별자
     * @param roleName 권한 이름
     * @return JWT 클레임 맵
     */
    private Map<String, Object> createClaims(Long memberId,
                                             String roleName) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        claims.put("role", roleName);
        return claims;
    }

    /**
     * 리프레시 토큰을 Redis 에 저장합니다.
     * 예시: key - refreshtoken:1(memberId) value - 실제 토큰 값
     *
     * @param refreshToken 리프레시 토큰
     * @param memberId     회원 식별자
     */
    private void storeRefreshTokenInRedis(String refreshToken,
                                          Long memberId) {

        String key = REFRESH_TOKEN_PREFIX + memberId;

        redisTemplate.opsForValue().set(key, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS);

        // TODO 토큰값을 DB 에 저장하는 로직 추가, redis 에서 삭제 -> 이후 DB 반영
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
     * @param memberId redis 삭제 객체
     */
    public void invalidateTokenAndDeleteRedisRefreshToken (HttpServletResponse response, Long memberId) {
        if (response == null) return;

        deleteTokenInCookie(response, REFRESH_TOKEN);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
    }

    /**
     * 쿠키에서 해당 토큰을 삭제합니다.
     *
     * @param response HttpServletResponse 객체
     */
    public void deleteTokenInCookie(HttpServletResponse response, String cookieName) {
        if (response == null) return;

        Cookie expiredCookie = new Cookie(cookieName, null);
        expiredCookie.setMaxAge(0); // 쿠키 즉시 삭제
        expiredCookie.setPath("/"); // 전체 경로에 적용

        response.addCookie(expiredCookie);

        log.debug("토큰 쿠키가 삭제되었습니다. 쿠키 이름: {}, Value: {}",
                expiredCookie.getName(), expiredCookie.getValue());
    }

    /**
     * 블랙리스트에 토큰을 추가하는 메서드입니다.
     * 토큰과 만료 시간을 저장하여 이후 검증 시 사용합니다.
     *
     * @param token 블랙리스트에 추가할 토큰 문자열
     */
    public void addToBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) return;

        long expirationTime = System.currentTimeMillis() + refreshExpiration;
        blacklist.put(token, expirationTime);
        log.info("블랙리스트에 토큰 추가: {}, 만료 시간: {}", token, expirationTime);
    }
}