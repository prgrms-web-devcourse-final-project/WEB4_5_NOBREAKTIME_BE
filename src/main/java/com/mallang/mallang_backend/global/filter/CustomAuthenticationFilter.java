package com.mallang.mallang_backend.global.filter;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.mallang.mallang_backend.global.constants.AppConstants.EXCLUDE_PATH_PATTERNS;
import static com.mallang.mallang_backend.global.constants.AppConstants.STATIC_RESOURCES_REGEX;
import static com.mallang.mallang_backend.global.exception.ErrorCode.IN_BLACKLIST;
import static com.mallang.mallang_backend.global.exception.ErrorCode.TOKEN_NOT_FOUND;

/**
 * 사용자 검증 필터
 * <p>
 * access token -> header
 * refresh token -> cookie (session)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        log.info("CustomAuthenticationFilter 실행");

        try {
            // request 에서 헤더 추출, 헤더에 값이 없으면 더이상 진행하지 않음
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 액세스 토큰이 유효한 것인지 검증하고 토큰에서 사용자 정보 추출
            String accessToken = extractTokenFromHeader(request);

            try {
                Claims claims = jwtService.parseAndValidateToken(accessToken);
                handleJwt(claims);
                filterChain.doFilter(request, response);
            } catch (ServiceException e) {
                handleTokenExpiration(request, response, filterChain);
            }
        } catch (Exception e) {
            log.error("내부 서버 오류: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("서버 오류 발생");
        }
    }

    private void handleTokenExpiration(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        log.info("토큰 만료, 재발급 로직 시작");

        String newAccessToken = getNewAccessToken(request);
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        log.info("재발급 된 access token: {}", newAccessToken);

        Claims newClaims = jwtService.parseAndValidateToken(newAccessToken);
        if (newClaims != null) {
            handleJwt(newClaims);
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("인증되지 않은 사용자입니다.");
        }
    }

    /**
     * 액세스 토큰 만료 시, 리프레시 토큰으로 액세스 토큰 재발급
     *
     * @param request 리프레시 토큰을 가져오기 위한 파라미터
     * @return 재발급 된 액세스 토큰
     */
    private String getNewAccessToken(HttpServletRequest request) {
        String refreshToken = jwtService.getTokenByCookieName(request)
                .orElseThrow(() -> new ServiceException(TOKEN_NOT_FOUND));

        // 블랙리스트 체크
        checkBlackList(refreshToken);

        Claims claims = jwtService.parseAndValidateToken(refreshToken);
        Long memberId = ((Integer) claims.get("memberId")).longValue();
        String roleName = (String) claims.get("role");

        return tokenService.createAccessToken(memberId, roleName);
    }

    /**
     * 토큰이 블랙 리스트에 들어있는 토큰인지 검증하기 위한 메서드
     *
     * @param refreshToken 리프레시 토큰 -> 사용 기간이 긴 만큼 블랙 리스트로 관리
     */
    private void checkBlackList(String refreshToken) {
        // 블랙리스트 확인
        if (tokenService.isTokenBlacklisted(refreshToken)) {
            throw new ServiceException(IN_BLACKLIST);
        }
    }

    /**
     * 헤더에서 Bearer 토큰을 꺼내기 위한 메서드
     *
     * @param request 헤더 추출을 위한 파라미터
     * @return 액세스 토큰 값
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰 부분만 반환
        }
        return null;
    }

    /**
     * claims 에서 사용자 정보를 추출해 인증 객체 설정
     *
     * @param claims -> memberId, roleName이 들어있음
     */
    private void handleJwt(Claims claims) {
        Long memberId = ((Integer) claims.get("memberId")).longValue();
        String roleName = (String) claims.get("role");
        setAuthentication(memberId, roleName); // 인증 객체 설정
    }

    /**
     * 커스텀 인증 객체 CustomUserDetails 에 Jwt 에서 파싱한 값을 넣어 주기 위한 메서드
     *
     * @param memberId 사용자 고유 id 값 PK
     * @param roleName 사용자 구독 설정에 따른 권한 값
     */
    private void setAuthentication(Long memberId, String roleName) {
        CustomUserDetails customUserDetails = new CustomUserDetails(memberId, roleName);

        // 인증 완료 후 민감 데이터 제거를 위해 Credentials 필드를 null 로 처리
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("인증 성공: {}", authentication);
    }

    /**
     * 필터가 적용되면 안 되는 경로만 따로 상수 처리
     * @param request
     * @return 적용되는 경로에 대해 true 반환
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // OPTIONS 메서드라면 필터 적용 안 함
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();

        if (isExcludedPath(request)) {
            return true;
        }

        return requestURI.startsWith("/h2-console") ||
                requestURI.matches(STATIC_RESOURCES_REGEX);
    }

    // 경로 제외 여부 확인
    private boolean isExcludedPath(HttpServletRequest request) {
        RequestMatcher matcher = new OrRequestMatcher(
                Arrays.stream(EXCLUDE_PATH_PATTERNS)
                        .map(AntPathRequestMatcher::new)
                        .toArray(RequestMatcher[]::new)
        );
        return matcher.matches(request);
    }
}
