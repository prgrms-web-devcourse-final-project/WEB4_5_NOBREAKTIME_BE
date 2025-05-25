package com.mallang.mallang_backend.domain.member.oauth.service;

import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.oauth.processor.OAuth2UserProcessor;
import com.mallang.mallang_backend.domain.member.service.main.MemberService;
import com.mallang.mallang_backend.domain.member.service.withdrawn.MemberWithdrawalService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 로그인 서비스 처리 (트랜잭션 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final MemberService memberService;
    private final List<OAuth2UserProcessor> processors;
    private final MemberWithdrawalService withdrawalService;
    private final S3ImageUploader imageUploader;

    public OAuth2User processLogin(LoginPlatform platform,
                                   OAuth2User user) {
        Map<String, Object> userAttributes = parseUserAttributes(platform, user);
        String platformId = user.getName();
        log.debug("platformId: {}", platformId);

        if (memberService.existsByPlatformId(platformId)) {
            handleExistingMember(platformId);
        } else {
            registerNewMember(platform, userAttributes);
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
    public void handleExistingMember(String platformId) {

        Member member = memberService.findByPlatformId(platformId);

        if (member.getPlatformId() == null) {
            throw new ServiceException(MEMBER_ALREADY_WITHDRAWN);
        }

        log.info("이미 존재하는 회원: {}", platformId);
    }

    /**
     * 신규 회원을 등록합니다.
     *
     * @param platform       로그인 플랫폼 정보
     * @param userAttributes 사용자 속성 정보 (플랫폼에서 전달)
     * @throws ServiceException 30일 이내 탈퇴 이력이 있을 경우 예외 발생
     */
    public void registerNewMember(LoginPlatform platform,
                                  Map<String, Object> userAttributes) {

        // 필수 속성 추출
        String platformId = (String) userAttributes.get(PLATFORM_ID_KEY);

        String email = (String) userAttributes.get("email");
        String originalNickname = (String) userAttributes.get(NICKNAME_KEY);
        String profileImage = (String) userAttributes.get(PROFILE_IMAGE_KEY);

        // 닉네임 중복 방지 로직 적용
        String nickname = generateUniqueNickname(originalNickname);

        log.debug("사용자 platformId: {}, email: {}, nickname: {}, profileImage: {}",
                platformId, email, nickname, profileImage);

        // 프로필 이미지 S3 업로드
        String s3ProfileImageUrl = uploadProfileImage(profileImage);

        memberService.signupByOauth(
                platformId,
                email,
                originalNickname,
                s3ProfileImageUrl,
                platform
        );
    }

    /**
     * 프로필 이미지를 S3에 업로드하고 URL을 반환합니다.
     *
     * @param profileImageUrl 업로드할 이미지 URL
     * @return S3에 업로드된 이미지 URL
     */
    private String uploadProfileImage(String profileImageUrl) {
        ImageUploadRequest request = new ImageUploadRequest(profileImageUrl);
        return imageUploader.uploadImageURL(request);
    }

    /**
     * 원본 닉네임을 기반으로 고유한 닉네임을 생성합니다.
     * - 중복 시 랜덤 접미사(2~3자리)를 추가
     * - 최대 5회 시도 후 예외 발생
     *
     * @param originalNickname 사용자가 입력한 원본 닉네임
     * @return 사용 가능한 고유 닉네임
     * @throws ServiceException 유일한 닉네임 생성 실패 시
     */
    private String generateUniqueNickname(String originalNickname) {
        final int MAX_ATTEMPTS = 5;
        String currentNickname = originalNickname;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            if (!memberService.existsByNickname(currentNickname)) {
                return currentNickname;
            }
            String randomSuffix = RandomStringGenerator.generate(2 + new SecureRandom().nextInt(2));
            currentNickname = originalNickname + randomSuffix;
        }

        log.error("닉네임 생성 실패: {}", originalNickname);
        throw new ServiceException(NICKNAME_GENERATION_FAILED);
    }
}
