package com.mallang.mallang_backend.domain.video.learning.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoLearningExpressionQuizItem {
	private String question;      // 원문
	private List<String> choices; // 구두점 제거 + 셔플한 단어 목록
	private String meaning;        // 정답(원문)

	/**
	 * Expression 엔티티와 Random을 받아서
	 * 순서맞추기 퀴즈 아이템으로 변환하는 팩토리 메서드
	 */
	public static VideoLearningExpressionQuizItem of(
		com.mallang.mallang_backend.domain.sentence.expression.entity.Expression expr,
		Random random
	) {
		var sentence = expr.getSentence();
		var description = expr.getDescription();

		var words = Arrays.stream(sentence.split("\\s+"))
			.map(w -> w.replaceAll("\\p{Punct}", ""))
			.collect(Collectors.toList());
		Collections.shuffle(words, random);

		return VideoLearningExpressionQuizItem.builder()
			.question(sentence)
			.choices(words)
			.meaning(description)
			.build();
	}
}
