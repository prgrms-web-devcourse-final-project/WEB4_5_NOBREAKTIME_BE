package com.mallang.mallang_backend.domain.member.service.impl;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.Subscription;
import com.mallang.mallang_backend.domain.member.query.MemberQueryRepository;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.domain.member.service.SubscriptionService;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class MemberServiceImplTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    MemberQueryRepository memberQueryRepository;

    @Autowired
    S3ImageUploader imageUploader;

    @Autowired
    EntityManager em;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    WordbookRepository wordbookRepository;

    @Autowired
    private MemberService memberService;

    // 공통 초기화 데이터
    private Member createMember1() {
        return Member.builder()
                .platformId("123123A")
                .email("test1@example.com")
                .nickname("testUser1")
                .loginPlatform(LoginPlatform.GOOGLE)
                .language(Language.NONE)
                .profileImageUrl("profile.jpg")
                .build();
    }

    private Member createMember2() {
        return Member.builder()
                .platformId("1231212B")
                .email("test2@example.com")
                .nickname("testUser2")
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.NONE)
                .profileImageUrl("profile.jpg")
                .build();
    }

    @Test
    @Transactional
    @DisplayName("소셜 로그인 성공 케이스")
    void signupByOauth_Success() {
        // Given
        Long memberId = memberService.signupByOauth(
                "23123412A",
                "oauth@example.com",
                "oauthUser",
                "profileUrl",
                LoginPlatform.GOOGLE
        );

        // When & Then: 멤버 저장 확인
        Member savedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(savedMember.getId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("소셜 로그인 유저 언어 추가 성공")
    void updateLearningLanguage_Success() {
        // Given
        Member member = createMember1();
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
    void getRoleName_Success() {
        // Given: 구독 정보 포함한 멤버 저장
        Member member = createMember1();
        Member savedMember = memberRepository.save(member);
        savedMember.updateSubscription(Subscription.PREMIUM);

        // When
        String role = memberService.getRoleName(savedMember.getId());

        // Then
        assertEquals("ROLE_PREMIUM", role);
    }

    @Test
    @DisplayName("6개월 후 자동 삭제 로직 검증")
    void bulkDeleteExpiredMembers() throws Exception {
        //given
        Member m1 = createMember1();
        m1.updateWithdrawalDate(LocalDateTime.now().minusMonths(7));
        Member expiredMember = memberRepository.save(m1);
        log.info("expiredMember: {}", expiredMember.getId());

        Member m2 = createMember2();
        Member activeMember = memberRepository.save(m2);
        m2.updateWithdrawalDate(LocalDateTime.now().minusMonths(5));
        log.info("activeMember: {}", activeMember.getId());

        //when
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
        long deletedCount = memberQueryRepository.bulkDeleteExpiredMembers(threshold);

        //then
        assertThat(deletedCount).isEqualTo(1);
        // assertThat(memberRepository.findAll()).hasSize(1);
    }
}