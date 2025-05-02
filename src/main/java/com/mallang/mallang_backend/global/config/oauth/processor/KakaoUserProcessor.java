package com.mallang.mallang_backend.global.config.oauth.processor;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.ID_KEY;
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
        String id = String.valueOf(attributes.get(ID_KEY));
        Map<String, Object> properties = extractProperties(attributes);
        properties.put(ID_KEY, id);

        log.info("Kakao Properties : {}", properties);
        return properties;
    }

    /**
     * 카카오 OAuth2 응답에서 properties 맵을 추출
     * @param attributes 전체 속성 맵
     * @return nickname, profile_image_url 등이 포함된 맵
     */
    private Map<String, Object> extractProperties(Map<String, Object> attributes) {
        Object propObj = attributes.get("properties");

        if (propObj instanceof Map<?, ?> rawMap) {
            return convertToStringObjectMap(rawMap);
        }
        throw new ServiceException(INVALID_ATTRIBUTE_MAP);
    }

    /**
     * Map<?, ?>를 Map<String, Object>로 안전하게 변환
     */
    private Map<String, Object> convertToStringObjectMap(Map<?, ?> rawMap) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String key) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }
}