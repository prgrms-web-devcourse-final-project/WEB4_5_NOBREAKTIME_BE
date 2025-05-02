package com.mallang.mallang_backend.domain.quiz.expressionquiz.controller;

import java.util.List;

import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionQuizResponse {
	private Long quizId;
	List<ExpressionQuizItem> quizItems;
}
