package com.mallang.mallang_backend.domain.quiz.expressionquiz.entity;

import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 표현 퀴즈
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_quiz_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType;

    @Column(nullable = false)
    private Integer runningTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    // 생성 메서드
    @Builder
    public ExpressionQuiz(QuizType quizType,
                          Integer runningTime,
                          Language language) {
        this.quizType = quizType;
        this.runningTime = runningTime;
        this.language = language;
    }
}