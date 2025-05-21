package com.mallang.mallang_backend.global.filter.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomUserDetails 단위 테스트")
class CustomUserDetailsTest {

    @Test
    @DisplayName("권한이 ROLE_STANDARD이면 SimpleGrantedAuthority 생성 확인")
    void getAuthorities_shouldReturnCorrectAuthority() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "ROLE_STANDARD");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_STANDARD")));
    }

    @Test
    @DisplayName("패스워드는 빈 문자열 반환")
    void getPassword_shouldReturnEmpty() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "ROLE_STANDARD");
        assertEquals("", userDetails.getPassword());
    }

    @Test
    @DisplayName("유저네임은 빈 문자열 반환")
    void getUsername_shouldReturnEmpty() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "ROLE_STANDARD");
        assertEquals("", userDetails.getUsername());
    }

    @Test
    @DisplayName("toString 메서드가 필드값 포함하는지 확인")
    void toString_shouldContainFields() {
        CustomUserDetails userDetails = new CustomUserDetails(123L, "ROLE_PREMIUM");
        String result = userDetails.toString();

        assertTrue(result.contains("123"));
        assertTrue(result.contains("ROLE_PREMIUM"));
    }
}
