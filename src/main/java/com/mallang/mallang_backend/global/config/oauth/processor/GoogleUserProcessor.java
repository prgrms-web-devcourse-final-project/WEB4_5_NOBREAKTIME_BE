package com.mallang.mallang_backend.global.config.oauth.processor;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

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
        newAttributes.put(PLATFORM_ID_KEY, attributes.get("sub"));

        // given_name -> nickname으로 매핑
        newAttributes.put(NICKNAME_KEY, attributes.get("given_name"));

        // picture -> profile_image로 매핑
        newAttributes.put(PROFILE_IMAGE_KEY, attributes.get("picture"));

        newAttributes.put("email", attributes.get("email"));

        log.debug("newAttributes = {}", newAttributes);
        return newAttributes;
    }
}
