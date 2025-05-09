package com.mallang.mallang_backend.global.filter.userfilter;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenPair;
import com.mallang.mallang_backend.global.token.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.TOKEN_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenFilter extends CustomAbstractFilter {

    private final TokenService tokenService;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 이미 인증된 경우 바로 넘김 (중복 인증 방지)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 리프레시 토큰 추출
            String refreshToken = jwtService.getTokenByCookie(request, REFRESH_TOKEN)
                    .orElseThrow(() -> new ServiceException(TOKEN_NOT_FOUND));

            // 2. 토큰 검증
            Claims claims = jwtService.parseAndValidateToken(refreshToken);

            // 3. Redis 에서 리프레시 토큰 존재 확인 (올바른 키 구조로 수정)
            Long memberId = ((Integer) claims.get("memberId")).longValue();
            String redisKey = REFRESH_TOKEN_PREFIX + memberId;
            Boolean exists = redisTemplate.hasKey(redisKey);

            if (exists == null || !exists) {
                throw new ServiceException(TOKEN_NOT_FOUND);
            }

            // 4. 인증 객체 생성
            JwtAuthUtils.handleJwt(claims);

            // 5. 새 토큰 발급 및 적용
            String roleName = (String) claims.get("role");
            TokenPair tokenPair = tokenService.createTokenPair(memberId, roleName);
            jwtService.setJwtSessionCookie(tokenPair.getAccessToken(), response);
            log.info("사용자 액세스 토큰: {}", tokenPair.getAccessToken());

            jwtService.setJwtPersistentCookie(tokenPair.getRefreshToken(), response);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            // 만료된 리프레시 토큰의 경우
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 오류 반환해 줄 것
        }
    }
}
