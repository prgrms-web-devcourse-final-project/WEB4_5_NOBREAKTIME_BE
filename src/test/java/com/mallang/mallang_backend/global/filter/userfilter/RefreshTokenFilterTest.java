package com.mallang.mallang_backend.global.filter.userfilter;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenPair;
import com.mallang.mallang_backend.global.token.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("RefreshTokenFilter 테스트")
public class RefreshTokenFilterTest {

    private JwtService jwtService;
    private TokenService tokenService;
    private RedisTemplate<String, Object> redisTemplate;
    private RefreshTokenFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        tokenService = mock(TokenService.class);
        redisTemplate = mock(RedisTemplate.class);
        filter = new RefreshTokenFilter(tokenService, jwtService, redisTemplate);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 정보가 이미 존재하면 바로 필터 체인 진행")
    void alreadyAuthenticated() throws ServletException, IOException {
        CustomUserDetails user = new CustomUserDetails(1L, "ROLE_STANDARD");
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 인증 및 새 토큰 발급")
    void validRefreshToken() throws Exception {
        request.setCookies(new jakarta.servlet.http.Cookie("refresh_token", "valid-token"));
        Claims claims = mock(Claims.class);
        when(claims.get("memberId")).thenReturn(1);
        when(claims.get("role")).thenReturn("ROLE_USER");

        when(jwtService.getTokenByCookie(request, "refresh_token"))
                .thenReturn(Optional.of("valid-token"));
        when(jwtService.parseAndValidateToken("valid-token"))
                .thenReturn(claims);
        when(redisTemplate.hasKey("refreshToken:1"))
                .thenReturn(true);

        TokenPair tokenPair = new TokenPair("access-token", "refresh-token");
        when(tokenService.createTokenPair(1L, "ROLE_USER")).thenReturn(tokenPair);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 없음 -> 예외 발생")
    void noTokenProvided() throws Exception {
        when(jwtService.getTokenByCookie(request, "refresh_token"))
                .thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> {
            filter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    @DisplayName("Redis에 토큰 없음 -> 예외 발생")
    void tokenNotInRedis() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.get("memberId")).thenReturn(1);
        when(jwtService.getTokenByCookie(request, "refresh_token"))
                .thenReturn(Optional.of("valid-token"));
        when(jwtService.parseAndValidateToken("valid-token"))
                .thenReturn(claims);
        when(redisTemplate.hasKey("refreshToken:1"))
                .thenReturn(false);

        assertThrows(ServiceException.class, () -> {
            filter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    @DisplayName("리프레시 토큰 만료 시 401 반환")
    void expiredToken() throws Exception {
        when(jwtService.getTokenByCookie(request, "refresh_token"))
                .thenReturn(Optional.of("expired-token"));
        when(jwtService.parseAndValidateToken("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "Expired"));

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }
}
