package com.mallang.mallang_backend.global.filter.userfilter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CustomAbstractFilter 테스트")
class CustomAbstractFilterTest {

    // 테스트를 위한 더미 필터 구현체
    static class TestFilter extends CustomAbstractFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain) {
            // noop
        }
    }

    private final CustomAbstractFilter filter = new TestFilter();

    @Nested
    @DisplayName("shouldNotFilter 테스트")
    class ShouldNotFilter {

        @Test
        @DisplayName("/h2-console 경로는 필터 제외")
        void excludeH2Console() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/h2-console/some/path");
            when(request.getServletPath()).thenReturn("/h2-console/some/path");

            assertTrue(filter.shouldNotFilter(request));
        }

        @Test
        @DisplayName("정적 자원 경로는 필터 제외")
        void excludeStaticResources() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/css/style.css");
            when(request.getServletPath()).thenReturn("/css/style.css");

            assertTrue(filter.shouldNotFilter(request));
        }

        @Test
        @DisplayName("EXCLUDE_PATH_PATTERNS 내 경로는 필터 제외")
        void excludeDefinedPaths() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/plans/123");
            when(request.getServletPath()).thenReturn("/api/v1/plans/123");

            assertTrue(filter.shouldNotFilter(request));
        }

        @Test
        @DisplayName("필터가 적용되어야 할 경로")
        void filterAppliedPath() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/v1/secure/resource");
            when(request.getServletPath()).thenReturn("/api/v1/secure/resource");

            assertFalse(filter.shouldNotFilter(request));
        }
    }
}
