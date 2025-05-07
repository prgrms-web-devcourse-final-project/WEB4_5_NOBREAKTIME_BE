package com.mallang.mallang_backend.domain.quiz.expressionquiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionQuizResultSaveRequest {
	Long quizId;
	Long expressionBookId;
	Long expressionId;
	boolean isCorrect;
}
