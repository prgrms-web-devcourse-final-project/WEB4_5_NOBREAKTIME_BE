package com.mallang.mallang_backend.global.config.oauth.processor;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.mallang.mallang_backend.global.exception.ErrorCode.INVALID_ATTRIBUTE_MAP;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverUserProcessor implements OAuth2UserProcessor {

    @Override
    public boolean supports(LoginPlatform loginPlatform) {
        return loginPlatform == LoginPlatform.NAVER;
    }

    @Override
    public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
        return extractResponse(attributes);
    }

    /**
     * 네이버 OAuth2 응답에서 response 맵 추출
     * @param attributes 전체 속성 맵
     * @return nickname, profile_image 등이 포함된 맵
     */
    private Map<String, Object> extractResponse(Map<String, Object> attributes) {
        Object propObj = attributes.get("response");

        if (propObj instanceof Map<?, ?> rawMap) {
            Map<String, Object> responseMap = convertToStringObjectMap(rawMap);
            log.info("Naver response: {}", responseMap);
            return responseMap;
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