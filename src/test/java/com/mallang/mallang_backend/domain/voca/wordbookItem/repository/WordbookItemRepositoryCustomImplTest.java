package com.mallang.mallang_backend.domain.voca.wordbookItem.repository;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepositoryCustomImpl;
import com.mallang.mallang_backend.global.common.Language;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Transactional
class WordbookItemRepositoryCustomImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private JPAQueryFactory queryFactory;

    private WordbookItemRepositoryCustomImpl repository;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        repository = new WordbookItemRepositoryCustomImpl(queryFactory);
    }

    @Test
    @DisplayName("복습 대상 단어 조회 - 복습 주기가 지난 단어만 반환됨")
    void testFindReviewTargetWords() {
        // given
        Member member = Member.builder()
                .email("user@test.com")
                .nickname("tester")
                .loginPlatform(LoginPlatform.GOOGLE)
                .language(Language.ENGLISH)
                .build();
        em.persist(member);

        Wordbook wordbook = Wordbook.builder()
                .member(member)
                .name("테스트 단어장")
                .language(Language.ENGLISH)
                .build();
        em.persist(wordbook);

        WordbookItem reviewTarget = WordbookItem.builder()
                .wordbook(wordbook)
                .word("hello")
                .build();
        reviewTarget.updateStatus(WordStatus.WRONG);
        reviewTarget.updateLastStudiedAt(now.minusDays(2));
        em.persist(reviewTarget);

        WordbookItem newWord = WordbookItem.builder()
                .wordbook(wordbook)
                .word("banana")
                .build();
        em.persist(newWord);

        em.flush();
        em.clear();

        // when
        List<WordbookItem> result = repository.findReviewTargetWords(member, now);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWord()).isEqualTo("hello");
    }
}