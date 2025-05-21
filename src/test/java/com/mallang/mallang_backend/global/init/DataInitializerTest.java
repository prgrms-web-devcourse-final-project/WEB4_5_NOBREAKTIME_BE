package com.mallang.mallang_backend.global.init;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("DataInitializer 통합 테스트")
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WordbookRepository wordbookRepository;

    @Autowired
    private ExpressionBookRepository expressionBookRepository;

    @BeforeEach
    void clean() {
        SecurityContextHolder.clearContext();
        expressionBookRepository.deleteAll();
        wordbookRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("초기화 실행 시 테스트 회원, 기본 단어장과 표현함 생성 및 인증 설정 확인")
    void dataInit_run_createsTestUserAndSetsAuth() throws Exception {
        // when
        dataInitializer.run();

        // then
        Member member = memberRepository.findByEmail("google123@gmail.com").orElseThrow();

        assertThat(member.getNickname()).isEqualTo("TestUser1");
        assertThat(wordbookRepository.findAllByMember(member)).isNotEmpty();
        assertThat(expressionBookRepository.findAllByMember(member)).isNotEmpty();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(principal).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("JWT 토큰 생성이 성공적으로 이루어진다")
    void createToken_success() {
        String token = dataInitializer.createToken();

        assertThat(token).isNotNull();
        assertThat(token).contains(".");
    }
}
