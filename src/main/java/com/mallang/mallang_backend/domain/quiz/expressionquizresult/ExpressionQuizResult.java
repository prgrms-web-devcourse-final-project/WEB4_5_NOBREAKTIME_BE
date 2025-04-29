package com.mallang.mallang_backend.domain.quiz.expressionquizresult;

import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 표현함 퀴즈 결과
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionQuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_quiz_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ExpressionBook_id", nullable = false)
    private ExpressionBook ExpressionBook;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expression_quiz_id", nullable = false)
    private ExpressionQuiz expressionQuiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expression_id", nullable = false)
    private Expression expression;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 생성 메서드
    @Builder
    public ExpressionQuizResult(
        ExpressionBook ExpressionBook,
        Expression expression,
        ExpressionQuiz expressionQuiz,
        Boolean isCorrect,
        Language language
    ) {
        this.ExpressionBook = ExpressionBook;
        this.expression = expression;
        this.expressionQuiz = expressionQuiz;
        this.isCorrect = isCorrect;
        this.language = language;
    }
}