package com.mallang.mallang_backend.domain.quiz.expressionquiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionQuizResultSaveRequest {
	private Long quizId;
	private Long expressionBookId;
	private Long expressionId;
	private boolean correct;
}
