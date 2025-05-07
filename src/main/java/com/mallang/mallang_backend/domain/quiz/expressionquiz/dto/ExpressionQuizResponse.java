package com.mallang.mallang_backend.domain.quiz.expressionquiz.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionQuizResponse {
	private Long quizId;
	List<ExpressionQuizItem> quizItems;
}
