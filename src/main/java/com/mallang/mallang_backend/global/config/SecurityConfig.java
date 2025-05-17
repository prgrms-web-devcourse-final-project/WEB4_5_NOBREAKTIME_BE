package com.mallang.mallang_backend.global.config;

import com.mallang.mallang_backend.global.config.oauth.CustomOAuth2SuccessHandler;
import com.mallang.mallang_backend.global.config.oauth.service.CustomOAuth2Service;
import com.mallang.mallang_backend.global.filter.userfilter.AccessTokenFilter;
import com.mallang.mallang_backend.global.filter.userfilter.RefreshTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * oauth2Login
 * /login/oauth2/code/{소셜 로그인 제공자} 경로로 콜백이 들어오면 인증 처리
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2Service customOAuth2Service;
    private final AccessTokenFilter accessTokenFilter;
    private final RefreshTokenFilter refreshTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/oauth2/**",
                                "/error",
                                "/h2-console/**",
                                "/api/v1/**",
                                "/health",
                                "/env",
                                "/v3/api-docs",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**").hasAnyRole(
                                "BASIC",
                                "STANDARD",
                                "PREMIUM",
                                "ADMIN")
                        .anyRequest().permitAll()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(accessTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(refreshTokenFilter, AccessTokenFilter.class)
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2Service)
                                )
                                .successHandler(customOAuth2SuccessHandler)
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 오리진 설정
        configuration.setAllowedOrigins(Arrays.asList(
                "https://cdpn.io",
                "https://*.mallang.site",
                "http://localhost:3000",
                "https://www.app4.qwas.shop",
                "https://login.aleph.kr"));
        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH"));
        // 자격 증명 허용 설정
        configuration.setAllowCredentials(true);
        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));
        // CORS 설정을 소스에 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
