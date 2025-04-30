package com.mallang.mallang_backend.domain.quiz.wordquiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordQuizResultSaveRequest {
	private Long quizId;
	private Long wordbookItemId;
	private String word;
	private Boolean isCorrect;
	private Long learningTime;
}