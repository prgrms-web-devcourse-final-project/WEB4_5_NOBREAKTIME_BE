package com.mallang.mallang_backend.domain.plan.entity.domain.quiz.expressionquiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpressionQuizItem {
	private Long expressionId;
	private Long expressionBookId;
	private String question;
	private String original;
	private List<String> choices;
	private String meaning;
}
