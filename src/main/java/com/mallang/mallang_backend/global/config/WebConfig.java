package com.mallang.mallang_backend.global.config;

import java.util.List;

import com.mallang.mallang_backend.global.interceptor.SentryUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mallang.mallang_backend.global.filter.login.LoginUserArgumentResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginUserArgumentResolver loginUserArgumentResolver;
    private final SentryUserInterceptor sentryUserInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 경로 요청에 대해 SentryUserInterceptor 적용
        registry.addInterceptor(sentryUserInterceptor)
                .addPathPatterns("/**");
    }

}