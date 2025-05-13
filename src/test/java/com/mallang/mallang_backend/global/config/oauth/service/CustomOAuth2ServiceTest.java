package com.mallang.mallang_backend.global.config.oauth.service;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLogRepository;
import com.mallang.mallang_backend.domain.member.service.main.MemberService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.CANNOT_SIGNUP_WITH_THIS_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2ServiceTest {

    @Test
    @DisplayName("탈퇴 후 30일 미만이면 ServiceException 발생")
    void registerNewMember_within30daysWithdrawn_throwsException() {
        // given
        MemberService memberService = Mockito.mock(MemberService.class);
        WithdrawnLogRepository logRepository = Mockito.mock(WithdrawnLogRepository.class);
        S3ImageUploader uploader = Mockito.mock(S3ImageUploader.class);

        CustomOAuth2Service service = new CustomOAuth2Service(
                memberService,
                null, // memberRepository
                Collections.emptyList(),
                uploader,
                logRepository
        );

        String testPlatformId = "test123";
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put(PLATFORM_ID_KEY, testPlatformId);
        userAttributes.put(NICKNAME_KEY, "testnick");
        userAttributes.put(PROFILE_IMAGE_KEY, "http://img.com/a.jpg");

        WithdrawnLog withdrawnLog = Mockito.mock(WithdrawnLog.class);

        // 최근 탈퇴(30일 미만)
        Mockito.when(logRepository.existsByOriginalPlatformId(testPlatformId)).thenReturn(true);
        Mockito.when(logRepository.findByOriginalPlatformId(testPlatformId)).thenReturn(withdrawnLog);
        Mockito.when(withdrawnLog.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(10));

        // when & then
        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.registerNewMember(LoginPlatform.KAKAO, userAttributes)
        );
        assertEquals(CANNOT_SIGNUP_WITH_THIS_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("탈퇴 후 30일이 경과하면 회원 가입이 정상 동작한다")
    void registerNewMember_after30daysWithdrawn_success() {
        // given
        MemberService memberService = Mockito.mock(MemberService.class);
        WithdrawnLogRepository logRepository = Mockito.mock(WithdrawnLogRepository.class);
        S3ImageUploader uploader = Mockito.mock(S3ImageUploader.class);

        CustomOAuth2Service service = new CustomOAuth2Service(
                memberService,
                null,
                Collections.emptyList(),
                uploader,
                logRepository
        );

        String testPlatformId = "test123";
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put(PLATFORM_ID_KEY, testPlatformId);
        userAttributes.put(NICKNAME_KEY, "testnick");
        userAttributes.put(PROFILE_IMAGE_KEY, "http://img.com/a.jpg");

        WithdrawnLog withdrawnLog = Mockito.mock(WithdrawnLog.class);

        // 탈퇴일이 40일 전 (30일 경과)
        Mockito.when(logRepository.existsByOriginalPlatformId(testPlatformId)).thenReturn(true);
        Mockito.when(logRepository.findByOriginalPlatformId(testPlatformId)).thenReturn(withdrawnLog);
        Mockito.when(withdrawnLog.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(40));
        Mockito.when(uploader.uploadImageURL(any())).thenReturn("https://s3-complete-url");

        // 닉네임 중복 검사 및 가입 정상화
        Mockito.when(memberService.isNicknameAvailable(anyString())).thenReturn(true);

        // when & then
        assertDoesNotThrow(() ->
                service.registerNewMember(LoginPlatform.KAKAO, userAttributes)
        );

        // 가입 성공: 실제로 signupByOauth이 호출됐는지 검증
        Mockito.verify(memberService, Mockito.times(1))
                .signupByOauth(anyString(), any(), any(), any(), any());
    }
}