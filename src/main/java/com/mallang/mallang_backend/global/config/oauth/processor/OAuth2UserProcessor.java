package com.mallang.mallang_backend.global.config.oauth.processor;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;

import java.util.Map;

/**
 * 소셜 로그인 플랫폼에 따라 실행할 수 있는 Processor 설정
 */
public interface OAuth2UserProcessor {

    boolean supports(LoginPlatform loginPlatform);
    Map<String, Object> parseAttributes(Map<String, Object> attributes);
}
