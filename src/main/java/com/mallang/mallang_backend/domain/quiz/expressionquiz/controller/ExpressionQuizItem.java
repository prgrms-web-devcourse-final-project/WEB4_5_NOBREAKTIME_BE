package com.mallang.mallang_backend.domain.quiz.expressionquiz.controller;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpressionQuizItem {
	private Long expressionQuizItemId;
	private String sentence;
	private List<String> words;
	private String meaning;
}
