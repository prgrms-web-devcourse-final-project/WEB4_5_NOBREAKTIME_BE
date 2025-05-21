package com.mallang.mallang_backend.domain.member.log.withdrawn;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.domain.member.service.main.MemberServiceImpl;
import com.mallang.mallang_backend.global.common.Language;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class WithdrawnLogTest {

    @Autowired
    private MemberServiceImpl memberService;

    @Autowired
    private WithdrawnLogRepository logRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private SubscriptionService subscriptionService;


    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .platformId("test123")
                .email("test@test.com")
                .nickname("t1")
                .language(Language.ENGLISH)
                .profileImageUrl("http://test.com/img.jpg")
                .loginPlatform(LoginPlatform.KAKAO)
                .build();

        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("회원 탈퇴 시 개인정보 마스킹 및 탈퇴 로그, 구독 처리까지 정상 수행")
    @Transactional
    void withdrawMemberSuccess() {
        // given: 기본 값 세팅
        Long memberId = testMember.getId();
        when(subscriptionService.hasActiveSubscription(eq(memberId))).thenReturn(true);

        // when: 회원 탈퇴
        memberService.withdrawMember(testMember.getId());

        // then
        // Member 개인정보 마스킹, 탈퇴일자 추가 확인
        Member withdrawn = memberRepository.findById(memberId).orElseThrow();
        assertThat(withdrawn.getPlatformId()).contains("withdrawn_");
        assertThat(withdrawn.getWithdrawalDate()).isNotNull();

        // 구독 다운그레이드 처리 호출 확인
        verify(subscriptionService, times(1)).hasActiveSubscription(eq(memberId));

        // 탈퇴 로그 저장 확인
        WithdrawnLog byOriginalPlatformId = logRepository.findByOriginalPlatformId("test123");
        assertThat(byOriginalPlatformId).isNotNull();
        assertThat(withdrawn.getId()).isEqualTo(byOriginalPlatformId.getMemberId());
    }
}