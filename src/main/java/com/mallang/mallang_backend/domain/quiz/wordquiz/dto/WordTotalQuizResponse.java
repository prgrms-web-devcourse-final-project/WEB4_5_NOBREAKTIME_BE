package com.mallang.mallang_backend.domain.quiz.wordquiz.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordTotalQuizResponse {
	private Long id;
	List<WordTotalQuizItem> quizItems;
}
