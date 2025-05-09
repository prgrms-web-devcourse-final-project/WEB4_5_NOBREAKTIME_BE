package com.mallang.mallang_backend.global.config.oauth.processor;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.INVALID_ATTRIBUTE_MAP;

/**
 * Kakao
 */
@Slf4j
@Component
public class KakaoUserProcessor implements OAuth2UserProcessor {

    @Override
    public boolean supports(LoginPlatform loginPlatform) {
        return loginPlatform == LoginPlatform.KAKAO;
    }

    @Override
    public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
        log.debug("KakaoUserProcessor.attributes {}", attributes);

        Map<String, Object> kakaoAccount = extractNestedMap(attributes, "kakao_account");
        Map<String, Object> properties = extractNestedMap(attributes, "properties");

        Map<String, Object> kakaoInfo = new HashMap<>();
        kakaoInfo.put(EMAIL_KEY, kakaoAccount.get("email"));
        kakaoInfo.put(NICKNAME_KEY, properties.get("nickname"));
        kakaoInfo.put(PROFILE_IMAGE_KEY, properties.get("profile_image"));
        kakaoInfo.put(PLATFORM_ID_KEY, String.valueOf(attributes.get("id")));

        log.debug("kakaoInfo {}", kakaoInfo);
        return kakaoInfo;
    }

    /**
     * 중첩 맵 추출 및 타입 변환 메서드 통합
     */
    private Map<String, Object> extractNestedMap(Map<String, Object> attributes, String key) {
        return Optional.ofNullable(attributes.get(key))
                .filter(obj -> obj instanceof Map<?, ?>)
                .map(obj -> (Map<?, ?>) obj)
                .map(this::convertToStringObjectMap)
                .orElseThrow(() -> new ServiceException(INVALID_ATTRIBUTE_MAP));
    }

    /**
     * 스트림을 이용한 간결한 맵 변환
     */
    private Map<String, Object> convertToStringObjectMap(Map<?, ?> rawMap) {
        return rawMap.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String)
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        Map.Entry::getValue
                ));
    }
}