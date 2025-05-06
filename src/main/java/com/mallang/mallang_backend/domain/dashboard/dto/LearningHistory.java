package com.mallang.mallang_backend.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LearningHistory {
	// 학습 시간
	String learningTime;
	// 푼 퀴즈 갯수
	int quizCount;
	// 학습한 영상 갯수
	int videoCount;
	// 추가한 단어 갯수
	int addedWordCount;
}
