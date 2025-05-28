package com.mallang.mallang_backend.domain.member.oauth.service;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.global.aop.monitor.MeasureExecutionTime;
import com.mallang.mallang_backend.global.aop.monitor.MonitorExternalApi;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import com.mallang.mallang_backend.global.metrics.CustomMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 성공 핸들러에서 사용자 정보를 받아 로그인 + DB 추가 과정을 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2Service extends DefaultOAuth2UserService {

    private final ExternalOAuth2UserService externalService;
    private final OAuthLoginService loginService;
    private final CustomMetricService metricService;

    @Override
    @TimeTrace
    @MonitorExternalApi(name = "OAuthLogin")
    @MeasureExecutionTime
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        metricService.recordLogin();
        String provider = request.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = externalService.loadUser(request); // 외부 API 호출 발생 지점 -> 격리

        return loginService.processLogin(LoginPlatform.from(provider.toLowerCase()), oAuth2User);
    }
}
