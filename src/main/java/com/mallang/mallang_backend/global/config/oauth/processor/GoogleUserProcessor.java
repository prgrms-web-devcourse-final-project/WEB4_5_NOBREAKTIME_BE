package com.mallang.mallang_backend.global.config.oauth.processor;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * google
 */
@Slf4j
@Component
public class GoogleUserProcessor implements OAuth2UserProcessor{
    @Override
    public boolean supports(LoginPlatform loginPlatform) {
        return loginPlatform == LoginPlatform.GOOGLE;
    }

    @Override
    public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
        Map<String, Object> newAttributes = new HashMap<>();

        // sub -> id로 매핑
        newAttributes.put("id", attributes.get("sub"));

        // given_name -> nickname으로 매핑
        newAttributes.put("nickname", attributes.get("given_name"));

        // picture -> profile_image로 매핑
        newAttributes.put("profile_image", attributes.get("picture"));

        log.info("newAttributes = {}", newAttributes);
        return newAttributes;
    }
}
