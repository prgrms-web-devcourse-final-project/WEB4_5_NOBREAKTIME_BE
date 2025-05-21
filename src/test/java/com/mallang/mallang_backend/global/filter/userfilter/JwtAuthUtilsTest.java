package com.mallang.mallang_backend.global.filter.userfilter;

import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthUtils 테스트")
class JwtAuthUtilsTest {

    @Test
    @DisplayName("handleJwt는 Claims에서 memberId, role을 파싱해 SecurityContext에 저장한다")
    void handleJwt_setsAuthenticationCorrectly() {
        // given
        Claims claims = Mockito.mock(Claims.class);
        when(claims.get("memberId")).thenReturn(42);
        when(claims.get("role")).thenReturn("ROLE_STANDARD");

        // when
        JwtAuthUtils.handleJwt(claims);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof CustomUserDetails);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        assertEquals(42L, userDetails.getMemberId());
        assertEquals("ROLE_STANDARD", userDetails.getRoleName());
        assertEquals("ROLE_STANDARD", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("setAuthentication은 SecurityContext에 인증 객체를 저장한다")
    void setAuthentication_setsSecurityContext() {
        // when
        JwtAuthUtils.setAuthentication(99L, "ROLE_PREMIUM");

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof CustomUserDetails);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        assertEquals(99L, userDetails.getMemberId());
        assertEquals("ROLE_PREMIUM", userDetails.getRoleName());
    }
}
