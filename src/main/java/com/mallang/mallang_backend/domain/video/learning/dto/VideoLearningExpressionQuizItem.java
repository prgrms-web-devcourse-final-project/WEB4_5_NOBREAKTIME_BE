package com.mallang.mallang_backend.domain.video.learning.dto;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Getter
@Builder
public class VideoLearningExpressionQuizItem {
	private final String question; // 빈칸 퀴즈용 문장 (단어 자리마다 {})
	private final String original; // 원문 문장
	private final List<String> choices; // 선택지 단어 목록
	private final String meaning; // 문장 해석

	/**
	 * Expression 엔티티와 Random을 받아 퀴즈 아이템으로 변환
	 */
	public static VideoLearningExpressionQuizItem of(
		Expression expr,
		Random random
	) {
		String sentence = expr.getSentence();
		String description = expr.getDescription();

		// 원문에서 단어만 추출 → 문장부호 제거 → 섞음
		List<String> words = Arrays.stream(sentence.split("\\s+"))
			.map(w -> w.replaceAll("\\p{Punct}", ""))
			.collect(Collectors.toList());
		Collections.shuffle(words, random);

		// 알파벳, 숫자(\w+)를 {}로 치환, 문장부호는 유지
		String blanked = Arrays.stream(sentence.split("\\s+"))
			.map(token -> token.replaceAll("[\\w'’]+", "{}"))
			.collect(Collectors.joining(" "));

		return VideoLearningExpressionQuizItem.builder()
			.question(blanked)
			.original(sentence)
			.choices(words)
			.meaning(description)
			.build();
	}
}
