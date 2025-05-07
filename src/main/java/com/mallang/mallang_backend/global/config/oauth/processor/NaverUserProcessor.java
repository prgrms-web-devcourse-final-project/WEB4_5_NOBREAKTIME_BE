package com.mallang.mallang_backend.global.config.oauth.processor;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.INVALID_ATTRIBUTE_MAP;

/**
 * 네이버 OAuth2 사용자 정보 파싱을 담당하는 프로세서입니다.
 *
 * 네이버 로그인 플랫폼의 OAuth2 응답에서 사용자 정보를 추출하고,
 * 표준화된 속성 맵으로 반환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverUserProcessor implements OAuth2UserProcessor {

    /**
     * 해당 프로세서가 지원하는 로그인 플랫폼인지 확인합니다.
     *
     * @param loginPlatform 로그인 플랫폼
     * @return 네이버 플랫폼일 경우 true, 아니면 false
     */
    @Override
    public boolean supports(LoginPlatform loginPlatform) {
        return loginPlatform == LoginPlatform.NAVER;
    }

    /**
     * 네이버 OAuth2 응답 속성에서 사용자 정보를 추출하여 반환합니다.
     *
     * @param attributes OAuth2 응답 전체 속성 맵
     * @return providerId, nickname, profile_image, email이 포함된 표준화된 맵
     */
    @Override
    public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
        Map<String, Object> responseMap = extractResponse(attributes);

        Map<String, Object> naverInfo = new HashMap<>();
        naverInfo.put(PLATFORM_ID_KEY, responseMap.get("id"));
        naverInfo.put(NICKNAME_KEY, responseMap.get("nickname"));
        naverInfo.put(PROFILE_IMAGE_KEY, responseMap.get("profile_image"));
        naverInfo.put("email", responseMap.get("email"));

        log.info("naverInfo: {}", naverInfo);
        return naverInfo;
    }

    /**
     * 네이버 OAuth2 응답에서 'response' 맵을 추출합니다.
     *
     * @param attributes 전체 속성 맵
     * @return nickname, profile_image 등이 포함된 맵
     * @throws ServiceException 'response' 필드가 없거나 형식이 올바르지 않은 경우
     */
    private Map<String, Object> extractResponse(Map<String, Object> attributes) {
        Object responseObj = attributes.get("response");

        if (responseObj instanceof Map<?, ?> rawMap) {
            Map<String, Object> responseMap = convertToStringObjectMap(rawMap);
            return responseMap;
        }
        throw new ServiceException(INVALID_ATTRIBUTE_MAP);
    }

    /**
     * Map<?, ?>를 Map<String, Object>로 변환합니다.
     *
     * @param rawMap 임의 타입의 키를 가진 맵
     * @return String 키와 Object 값의 맵
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
