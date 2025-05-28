package com.mallang.mallang_backend.domain.video.learning.dto;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.global.util.japanese.JapaneseSplitter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLearningExpressionQuizItem {
	private String question;  // 빈칸 퀴즈용 문장 (단어 자리마다 {})
	private String original;  // 원문 문장
	private List<String> choices; // 선택지 단어 목록
	private String meaning;   // 문장 해석

	/** 영어 퀴즈용 변환 메서드 */
	public static VideoLearningExpressionQuizItem fromSubtitleEnglish(
		Subtitle subtitle, Random random
	) {
		String sentence = subtitle.getOriginalSentence();
		String description = subtitle.getTranslatedSentence();

		String question = createQuestion(sentence);
		List<String> choices = parseWord(sentence, random);

		return VideoLearningExpressionQuizItem.builder()
			.question(question)
			.original(sentence)
			.choices(choices)
			.meaning(description)
			.build();
	}

	/** 일본어 퀴즈용 변환 메서드 */
	public static VideoLearningExpressionQuizItem fromSubtitleJapanese(
		Subtitle subtitle, Random random
	) {
		String raw = subtitle.getOriginalSentence();
		String description = subtitle.getTranslatedSentence();

		// 일본어 문장은 미리 띄어쓰기
		String sentence = JapaneseSplitter.splitJapanese(raw);
		String question = createQuestion(sentence);
		List<String> choices = parseWord(sentence, random);

		return VideoLearningExpressionQuizItem.builder()
			.question(question)
			.original(sentence)
			.choices(choices)
			.meaning(description)
			.build();
	}

	/**
	 * 알파벳·숫자(\w+)와 '’를 {}로 치환,
	 * 그 외 문자(한자·히라가나·카타카나 등)도 {}로 치환,
	 * 문장부호는 유지
	 */
	private static String createQuestion(String sentence) {
		return Arrays.stream(sentence.split("\\s+"))
			.map(token -> token.replaceAll("[\\w'’]+", "{}"))
			.map(token -> token.replaceAll("[\\p{L}\\p{N}'’ー々]+", "{}"))
			.collect(Collectors.joining(" "));
	}

	/**
	 * 1) 영어 기본 구두점 제거 ('’ 제외)
	 * 2) 일본어 특수 구두점 추가 제거
	 * 3) 빈 문자열 필터 → 셔플
	 */
	private static List<String> parseWord(String sentence, Random random) {
		// 문장 전체에서 '와 ’를 제외한 모든 구두점(영문·일문) 제거
		String cleaned = sentence.replaceAll(
			"[\\p{Punct}&&[^'’]]|[。、「」（）『』【】《》！？!?]+",
			""
		);

		// 공백으로 나눠서, 빈 문자열 필터 → 셔플
		List<String> words = Arrays.stream(cleaned.split("\\s+"))
			.filter(w -> !w.isBlank())
			.collect(Collectors.toList());

		Collections.shuffle(words, random);
		return words;
	}
}