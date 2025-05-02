package com.mallang.mallang_backend.domain.member.service.impl;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.Subscription;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.config.QueryDslConfig;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;



import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
@DataJpaTest
@Import({MemberServiceImpl.class, QueryDslConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberServiceImplTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WordbookRepository wordbookRepository;

    @Autowired
    private MemberService memberService;

    // 공통 초기화 데이터
    private Member createMember() {
        return Member.builder()
                .email("test@example.com")
                .nickname("testUser")
                .loginPlatform(LoginPlatform.GOOGLE)
                .language(Language.NONE)
                .profileImageUrl("profile.jpg")
                .build();
    }

    @Test
    @Transactional
    @DisplayName("소셜 로그인 성공 케이스")
    void signupByOauth_Success() {
        // When
        Long memberId = memberService.signupByOauth(
                "oauth@example.com",
                "oauthUser",
                "profileUrl",
                LoginPlatform.GOOGLE
        );

        // Then: 멤버 저장 확인
        Member savedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(savedMember.getId()).isEqualTo(memberId);

        // Then: 기본 단어장 생성 확인 (예: NONE, ENGLISH 등)
        Wordbook wordbook = wordbookRepository.findById(memberId).get();
        assertThat(wordbook).isNotNull();
    }

    @Test
    @DisplayName("소셜 로그인 유저 언어 추가 성공")
    void updateLearningLanguage_Success() {
        // Given
        Member member = createMember();
        Member savedMember = memberRepository.save(member);

        // When
        memberService.updateLearningLanguage(savedMember.getId(), Language.ENGLISH);

        // Then: DB에서 직접 조회하여 확인
        Member updatedMember = memberRepository.findById(savedMember.getId()).orElseThrow();
        assertEquals(Language.ENGLISH, updatedMember.getLanguage());
    }

    @Test
    @DisplayName("유저가 없을 때에는 언어 업데이트가 되지 않음")
    void updateLearningLanguage_UserNotFound_ThrowsException() {
        // When & Then
        assertThrows(ServiceException.class, () -> {
            memberService.updateLearningLanguage(999L, Language.ENGLISH);
        });
    }

    @Test
    @DisplayName("유저의 구독 정보 업데이트")
    void getSubscription_Success() {
        // Given: 구독 정보 포함한 멤버 저장
        Member member = createMember();
        Member savedMember = memberRepository.save(member);
        savedMember.updateSubscription(Subscription.PREMIUM);

        // When
        String role = memberService.getSubscription(savedMember.getId());

        // Then
        assertEquals("ROLE_PREMIUM", role);
    }
}