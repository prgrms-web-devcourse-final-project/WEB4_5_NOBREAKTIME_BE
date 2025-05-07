package com.mallang.mallang_backend.domain.quiz.wordquiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WordQuizResponse {
	private Long quizId;
	List<WordQuizItem> quizItems;
}
