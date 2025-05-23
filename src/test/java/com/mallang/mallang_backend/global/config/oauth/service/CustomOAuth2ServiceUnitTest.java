package com.mallang.mallang_backend.global.config.oauth.service;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLogRepository;
import com.mallang.mallang_backend.domain.member.oauth.processor.OAuth2UserProcessor;
import com.mallang.mallang_backend.domain.member.oauth.service.OAuthLoginService;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.CANNOT_SIGNUP_WITH_THIS_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2ServiceUnitTest {

    @Mock
    S3ImageUploader uploader;

    @Mock
    WithdrawnLogRepository logRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private List<OAuth2UserProcessor> processors;

    @Mock
    private WordbookRepository wordbookRepository;

    @Mock
    private ExpressionBookRepository expressionBookRepository;

    @InjectMocks
    OAuthLoginService service;

    @BeforeEach
    void setUp() {
        service = new OAuthLoginService(
                memberRepository,
                processors,
                uploader,
                logRepository,
                wordbookRepository,
                expressionBookRepository
        );

    }

    @Test
    @DisplayName("탈퇴 후 30일 미만이면 ServiceException 발생")
    void registerNewMember_within30daysWithdrawn_throwsException() {
        // given
        String testPlatformId = "test123";
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put(PLATFORM_ID_KEY, testPlatformId);
        userAttributes.put(NICKNAME_KEY, "testnick");
        userAttributes.put(PROFILE_IMAGE_KEY, "http://img.com/a.jpg");

        WithdrawnLog withdrawnLog = mock(WithdrawnLog.class);

        // 최근 탈퇴(30일 미만)
        when(logRepository.existsByOriginalPlatformId(testPlatformId)).thenReturn(true);
        when(logRepository.findByOriginalPlatformId(testPlatformId)).thenReturn(withdrawnLog);
        when(withdrawnLog.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(10));

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
        String testPlatformId = "test123";
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put(PLATFORM_ID_KEY, testPlatformId);
        userAttributes.put(NICKNAME_KEY, "testnick");
        userAttributes.put(PROFILE_IMAGE_KEY, "http://img.com/a.jpg");

        WithdrawnLog withdrawnLog = mock(WithdrawnLog.class);
        Member mockMember = mock(Member.class);

        // 탈퇴일이 40일 전 (30일 경과)
        when(logRepository.existsByOriginalPlatformId(testPlatformId)).thenReturn(true);
        when(logRepository.findByOriginalPlatformId(testPlatformId)).thenReturn(withdrawnLog);
        when(withdrawnLog.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(40));
        when(uploader.uploadImageURL(any())).thenReturn("https://s3-complete-url");
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(mockMember); // 추가
        when(mockMember.getId()).thenReturn(1L); // 추가

        // when & then
        assertDoesNotThrow(() ->
                service.registerNewMember(LoginPlatform.KAKAO, userAttributes)
        );

        // 가입 성공: 실제로 signupByOauth이 호출됐는지 검증
        verify(memberRepository, times(1)).save(any(Member.class));
    }
}