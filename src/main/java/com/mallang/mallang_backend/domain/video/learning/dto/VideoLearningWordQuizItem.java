package com.mallang.mallang_backend.domain.video.learning.dto;

import java.util.regex.Pattern;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.util.japanese.JapaneseSplitter;

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
	private String sentence;         // 빈칸 처리된 문장
	private String sentenceMeaning;  // Subtitle.translatedSentence

	/**
	 * Keyword 엔티티를 받아 빈칸 퀴즈 아이템으로 변환하는 팩토리 메서드
	 * - 일본어인 경우 JapaneseSplitter로 먼저 토큰화 후 첫 등장 단어만 치환
	 * - 그 외 언어는 기존 \b 경계를 이용해 단어 치환
	 */
	public static VideoLearningWordQuizItem from(Keyword k) {
		// 자막 정보 가져오기
		Subtitle sub = k.getSubtitles();
		Language lang = sub.getVideos() != null
			? sub.getVideos().getLanguage()
			: Language.ENGLISH;

		// 원문 문장 (토큰화된 문장 사용 여부 결정)
		String original = sub.getOriginalSentence();
		String forBlanking = lang == Language.JAPANESE
			? JapaneseSplitter.splitJapanese(original)
			: original;

		// 빈칸 처리
		String blanked;
		if (lang == Language.JAPANESE) {
			// 일본어는 토큰화된 문장에서 첫 단어 그대로 치환
			blanked = forBlanking.replaceFirst(
				Pattern.quote(k.getWord()),
				" {} "
			);
		} else {
			// 영어 등은 단어 경계(\b)로 안전하게 치환
			blanked = Pattern.compile(
					"\\b" + Pattern.quote(k.getWord()) + "\\b"
				)
				.matcher(forBlanking)
				.replaceFirst("{}");
		}

		return VideoLearningWordQuizItem.builder()
			.subtitleId(sub.getId())                       // 자막 ID
			.startTime(sub.getStartTime())                  // 자막 시작 시간
			.endTime(sub.getEndTime())                      // 자막 종료 시간
			.speaker(sub.getSpeaker())                      // 자막 화자
			.word(k.getWord())                              // 핵심 단어
			.meaning(k.getMeaning())                        // 단어 해석
			.sentence(blanked)                              // 빈칸 처리된 문장
			.sentenceMeaning(sub.getTranslatedSentence())   // 문장 해석
			.build();
	}
}
