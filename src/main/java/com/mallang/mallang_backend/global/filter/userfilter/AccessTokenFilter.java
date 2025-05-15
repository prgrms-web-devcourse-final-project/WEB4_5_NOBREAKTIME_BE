package com.mallang.mallang_backend.global.filter.userfilter;

import com.mallang.mallang_backend.global.token.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static com.mallang.mallang_backend.global.constants.AppConstants.ACCESS_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenFilter extends CustomAbstractFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 쿠키에서 accessToken 추출
            Optional<String> accessTokenOptional =
                    jwtService.getTokenByCookie(request, ACCESS_TOKEN);

            if (accessTokenOptional.isEmpty()) { // 토큰이 없는 경우
                filterChain.doFilter(request, response); // 다음 필터로 넘어감
                return;
            }

            String accessToken = accessTokenOptional.get();
            Claims userClaims = jwtService.parseAndValidateToken(accessToken);
            JwtAuthUtils.handleJwt(userClaims);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) { // 액세스 토큰 만료 시
            filterChain.doFilter(request, response); // RefreshTokenFilter 로 넘어감
        } catch (Exception e) { // 다른 예외 처리
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }
}
