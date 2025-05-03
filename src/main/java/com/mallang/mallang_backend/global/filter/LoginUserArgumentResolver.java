package com.mallang.mallang_backend.global.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    // 메서드에 @Login 애노테이션이 달려있는지 확인
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Login.class) &&
                CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new AuthenticationCredentialsNotFoundException("인증 정보가 잘못되었습니다.");
        }

        return principal;
    }
}
