package com.mallang.mallang_backend.domain.quiz.expressionquiz.dto;

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
	private String question;
	private String original;
	private List<String> choices;
	private String meaning;
}
