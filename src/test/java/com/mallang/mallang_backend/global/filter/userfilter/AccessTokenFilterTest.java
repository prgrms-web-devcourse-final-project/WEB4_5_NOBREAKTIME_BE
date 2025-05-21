package com.mallang.mallang_backend.global.filter.userfilter;

import com.mallang.mallang_backend.global.token.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static com.mallang.mallang_backend.global.constants.AppConstants.ACCESS_TOKEN;
import static org.mockito.Mockito.*;

@DisplayName("AccessTokenFilter 단위 테스트")
class AccessTokenFilterTest {

    JwtService jwtService = mock(JwtService.class);
    AccessTokenFilter filter = new AccessTokenFilter(jwtService);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    @BeforeEach
    void setUp() {
        reset(jwtService, request, response, filterChain);
    }

    @Test
    @DisplayName("액세스 토큰이 없으면 다음 필터로 전달")
    void doFilter_noToken() throws ServletException, IOException {
        when(jwtService.getTokenByCookie(request, ACCESS_TOKEN)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 액세스 토큰이 있으면 인증 처리 후 다음 필터로 전달")
    void doFilter_validToken() throws ServletException, IOException {
        String token = "valid.token";
        Claims claims = mock(Claims.class);

        when(jwtService.getTokenByCookie(request, ACCESS_TOKEN)).thenReturn(Optional.of(token));
        when(jwtService.parseAndValidateToken(token)).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).parseAndValidateToken(token);
    }

    @Test
    @DisplayName("액세스 토큰 만료 시 다음 필터로 전달")
    void doFilter_expiredToken() throws ServletException, IOException {
        String token = "expired.token";
        when(jwtService.getTokenByCookie(request, ACCESS_TOKEN)).thenReturn(Optional.of(token));
        when(jwtService.parseAndValidateToken(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 검증 중 다른 예외 발생 시 401 반환")
    void doFilter_invalidToken() throws ServletException, IOException {
        String token = "invalid.token";
        when(jwtService.getTokenByCookie(request, ACCESS_TOKEN)).thenReturn(Optional.of(token));
        when(jwtService.parseAndValidateToken(token)).thenThrow(new RuntimeException("Invalid token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
    }
}
