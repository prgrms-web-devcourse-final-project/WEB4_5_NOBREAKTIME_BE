package com.mallang.mallang_backend.global.filter.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LoginUserArgumentResolver 단위 테스트")
class LoginUserArgumentResolverTest {

    LoginUserArgumentResolver resolver = new LoginUserArgumentResolver();

    @Test
    @DisplayName("@Login 애노테이션과 CustomUserDetails 타입이면 supportsParameter는 true 반환")
    void supportsParameter_true() {
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.hasParameterAnnotation(Login.class)).thenReturn(true);
        when(parameter.getParameterType()).thenReturn((Class) CustomUserDetails.class);

        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    @DisplayName("Authentication이 null이면 예외 발생")
    void resolveArgument_authenticationNull() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                resolver.resolveArgument(null, null, null, null)
        );
    }

    @Test
    @DisplayName("Authentication principal이 CustomUserDetails가 아니면 예외 발생")
    void resolveArgument_invalidPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null)
        );

        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                resolver.resolveArgument(null, null, null, null)
        );
    }

    @Test
    @DisplayName("정상적인 principal이면 CustomUserDetails 반환")
    void resolveArgument_success() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "ROLE_STANDARD");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        Object result = resolver.resolveArgument(null, null, null, null);
        assertEquals(userDetails, result);
    }
}
