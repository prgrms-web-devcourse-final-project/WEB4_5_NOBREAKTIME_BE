package com.mallang.mallang_backend.domain.quiz.expressionquiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpressionQuizResponse {
	private Long quizId;
	private String expressionBookName;
	List<ExpressionQuizItem> quizItems;
}
