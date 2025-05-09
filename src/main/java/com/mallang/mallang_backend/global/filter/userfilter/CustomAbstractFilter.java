package com.mallang.mallang_backend.global.filter.userfilter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;

import static com.mallang.mallang_backend.global.constants.AppConstants.EXCLUDE_PATH_PATTERNS;
import static com.mallang.mallang_backend.global.constants.AppConstants.STATIC_RESOURCES_REGEX;

// 공통 필터 추상 클래스
public abstract class CustomAbstractFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/h2-console")
                || uri.matches(STATIC_RESOURCES_REGEX)
                || isExcludedPath(request);
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