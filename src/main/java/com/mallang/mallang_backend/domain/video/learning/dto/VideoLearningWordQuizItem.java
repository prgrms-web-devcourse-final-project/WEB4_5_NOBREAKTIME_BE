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
		Subtitle sub = k.getSubtitles();
		String original = sub.getOriginalSentence();
		String blanked = Pattern.compile("\\b" + Pattern.quote(k.getWord()) + "\\b")
			.matcher(original)
			.replaceFirst("{}");

		return VideoLearningWordQuizItem.builder()
			.subtitleId(sub.getId())
			.startTime(sub.getStartTime())
			.endTime(sub.getEndTime())
			.speaker(sub.getSpeaker())
			.word(k.getWord())
			.meaning(k.getMeaning())
			.sentence(blanked)
			.sentenceMeaning(sub.getTranslatedSentence())
			.build();
	}
}
