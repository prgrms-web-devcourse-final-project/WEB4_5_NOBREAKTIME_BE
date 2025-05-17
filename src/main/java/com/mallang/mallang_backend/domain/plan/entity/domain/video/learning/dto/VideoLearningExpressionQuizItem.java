package com.mallang.mallang_backend.domain.plan.entity.domain.video.learning.dto;

import com.mallang.mallang_backend.domain.plan.entity.domain.video.subtitle.entity.Subtitle;
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
	 * 자막 엔티티와 Random을 받아 퀴즈 아이템으로 변환
	 */
	public static VideoLearningExpressionQuizItem fromSubtitle(
		Subtitle subtitle,
		Random random
	) {
		String sentence = subtitle.getOriginalSentence();
		String description = subtitle.getTranslatedSentence();

		// 문장 부호 제거 단어 리스트
		List<String> words = Arrays.stream(sentence.split("\\s+"))
			.map(w -> w.replaceAll("\\p{Punct}", ""))  // 문장부호 제거
			.collect(Collectors.toList());
		Collections.shuffle(words, random);            // 랜덤 순서

		// 단어를 {}로 치환, 문장 부호 그대로
		String blanked = Arrays.stream(sentence.split("\\s+"))
			.map(token -> token.replaceAll("[\\w'’]+", "{}"))
			.collect(Collectors.joining(" "));

		return VideoLearningExpressionQuizItem.builder()
			.question(blanked)
			.original(sentence)
			.choices(words)
			.meaning(subtitle.getTranslatedSentence())
			.build();
	}
}
