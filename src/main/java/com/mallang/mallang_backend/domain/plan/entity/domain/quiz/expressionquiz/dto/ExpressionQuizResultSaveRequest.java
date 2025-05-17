package com.mallang.mallang_backend.domain.plan.entity.domain.quiz.expressionquiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionQuizResultSaveRequest {
	Long quizId;
	Long expressionBookId;
	Long expressionId;
	boolean correct;
}
