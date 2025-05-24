package com.mallang.mallang_backend.domain.member.oauth.service;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.oauth.processor.OAuth2UserProcessor;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 로그인 서비스 처리 (트랜잭션 적용)
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuthLoginService {

    private final MemberRepository memberRepository;
    private final List<OAuth2UserProcessor> processors;
    private final JoinService joinService;

    public OAuth2User processLogin(LoginPlatform platform,
                                   OAuth2User user) {
        Map<String, Object> userAttributes = parseUserAttributes(platform, user);
        String platformId = user.getName();
        log.debug("platformId: {}", platformId);

        if (memberRepository.existsByPlatformId(platformId)) {
            handleExistingMember(platformId);
        } else {
            joinService.registerNewMember(platform, userAttributes);
        }

        return new DefaultOAuth2User(Collections.emptyList(), userAttributes, "platformId");
    }

    // ======================회원 정보 추출=======================

    /**
     * 플랫폼별 사용자 속성 파싱 메서드
     *
     * @param platform 로그인 플랫폼 정보
     * @param user     OAuth2 인증 사용자 정보
     * @return 사용자 속성 맵
     */
    private Map<String, Object> parseUserAttributes(LoginPlatform platform,
                                                    OAuth2User user) {
        log.debug("user attributes {}", user.getAttributes());
        OAuth2UserProcessor processor = findSupportedProcessor(platform);
        return processor.parseAttributes(user.getAttributes());
    }

    private OAuth2UserProcessor findSupportedProcessor(LoginPlatform platform) {

        return processors.stream()
                .filter(p -> p.supports(platform))
                .findFirst()
                .orElseThrow(() -> new ServiceException(UNSUPPORTED_OAUTH_PROVIDER));
    }

    // ======================회원 로그인 & 가입 처리=======================

    /**
     * 플랫폼 ID로 기존 회원의 존재 여부를 비동기적으로 확인합니다.
     * 추후 필요에 따라 후속 작업(예: 로그, 알림 등)을 수행할 수 있습니다.
     *
     * @param platformId 확인할 회원의 플랫폼 ID
     */
    // @Async("securityTaskExecutor")
    private void handleExistingMember(String platformId) {

        Member member = memberRepository.findByPlatformId(platformId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        if (member.getPlatformId() == null) {
            throw new ServiceException(MEMBER_ALREADY_WITHDRAWN);
        }

        log.info("이미 존재하는 회원: {}", platformId);
    }
}
