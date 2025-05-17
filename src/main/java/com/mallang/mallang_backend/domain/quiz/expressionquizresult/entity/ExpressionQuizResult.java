package com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity;

import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 표현함 퀴즈 결과
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionQuizResult extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_quiz_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ExpressionBook_id", nullable = false)
    private ExpressionBook expressionBook;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expression_quiz_id", nullable = false)
    private ExpressionQuiz expressionQuiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expression_id", nullable = false)
    private Expression expression;

    @Column(nullable = false)
    private Boolean isCorrect;

    // 생성 메서드
    @Builder
    public ExpressionQuizResult(
        ExpressionBook expressionBook,
        Expression expression,
        ExpressionQuiz expressionQuiz,
        Boolean isCorrect
    ) {
        this.expressionBook = expressionBook;
        this.expression = expression;
        this.expressionQuiz = expressionQuiz;
        this.isCorrect = isCorrect;
    }

    /**
     * 퀴즈 결과의 표현의 표현함을 이동할 때 결과를 함께 변경하기 위해, 퀴즈 결과의 표현함도 변경한다.
     * @param targetBook 변경할 대상 표현함
     */
    public void updateExpressionBook(ExpressionBook targetBook) {
        this.expressionBook = targetBook;
    }
}