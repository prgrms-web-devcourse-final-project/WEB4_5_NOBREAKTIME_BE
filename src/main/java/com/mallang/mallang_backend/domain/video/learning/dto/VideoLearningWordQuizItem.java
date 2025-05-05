package com.mallang.mallang_backend.domain.video.learning.dto;

import java.util.regex.Pattern;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLearningWordQuizItem {
	private Long subtitleId;         // Subtitle.id
	private String startTime;        // Subtitle.startTime
	private String endTime;          // Subtitle.endTime
	private String speaker;          // Subtitle.speaker
	private String word;             // Keyword.word
	private String meaning;          // Keyword.meaning
	private String sentence;         // 빈칸 처리된 originalSentence
	private String sentenceMeaning;  // Subtitle.translatedSentence


	/**
	 * Keyword 엔티티를 받아 빈칸 퀴즈 아이템으로 변환하는 팩토리 메서드
	 */
	public static VideoLearningWordQuizItem from(Keyword k) {
		// 자막 정보 가져오기
		Subtitle sub = k.getSubtitles();

		// 원문 문장
		String original = sub.getOriginalSentence();

		// 단어를 {}로 치환
		String blanked = Pattern.compile("\\b" + Pattern.quote(k.getWord()) + "\\b")
			.matcher(original)
			.replaceFirst("{}");

		return VideoLearningWordQuizItem.builder()
			.subtitleId(sub.getId()) // 자막 ID
			.startTime(sub.getStartTime()) // 자막 시작 시간
			.endTime(sub.getEndTime()) // 자막 종료 시간
			.speaker(sub.getSpeaker()) // 자막 화자
			.word(k.getWord()) // 핵심 단어
			.meaning(k.getMeaning()) // 단어 해석
			.sentence(blanked) // 빈칸 처리된 문장
			.sentenceMeaning(sub.getTranslatedSentence()) // 문장 해석
			.build();
	}
}
