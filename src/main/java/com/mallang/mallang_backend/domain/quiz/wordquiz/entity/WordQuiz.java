package com.mallang.mallang_backend.domain.quiz.wordquiz.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_quiz_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType;

    private String learningTime; // 분, 초

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Builder
    public WordQuiz(
        Member member,
        QuizType quizType,
        String learningTime,
        Language language
    ) {
        this.member = member;
        this.quizType = quizType;
        this.learningTime = learningTime;
        this.language = language;
    }
}