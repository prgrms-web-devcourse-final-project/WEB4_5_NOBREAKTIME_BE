package com.mallang.mallang_backend.testfactory;

import java.util.List;

import org.mockito.Mockito;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizItem;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizItem;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;

public class VideoLearningTestFactory {

	public static Subtitle mockSubtitle(
		long id,
		String originalSentence,
		String translatedSentence,
		String startTime,
		String endTime,
		String speaker
	) {
		Subtitle sub = Mockito.mock(Subtitle.class);
		Mockito.lenient().when(sub.getId()).thenReturn(id);
		Mockito.lenient().when(sub.getOriginalSentence()).thenReturn(originalSentence);
		Mockito.lenient().when(sub.getTranslatedSentence()).thenReturn(translatedSentence);
		Mockito.lenient().when(sub.getStartTime()).thenReturn(startTime);
		Mockito.lenient().when(sub.getEndTime()).thenReturn(endTime);
		Mockito.lenient().when(sub.getSpeaker()).thenReturn(speaker);
		return sub;
	}

	public static Keyword mockKeyword(
		Subtitle subtitle,
		String word,
		String meaning
	) {
		Keyword keyword = Mockito.mock(Keyword.class);
		Mockito.lenient().when(keyword.getSubtitles()).thenReturn(subtitle);
		Mockito.lenient().when(keyword.getWord()).thenReturn(word);
		Mockito.lenient().when(keyword.getMeaning()).thenReturn(meaning);
		return keyword;
	}

	public static VideoLearningWordQuizItem createWordQuizItem(
		Long subtitleId,
		String startTime,
		String endTime,
		String speaker,
		String word,
		String meaning,
		String sentence,
		String sentenceMeaning
	) {
		return VideoLearningWordQuizItem.builder()
			.subtitleId(subtitleId)
			.startTime(startTime)
			.endTime(endTime)
			.speaker(speaker)
			.word(word)
			.meaning(meaning)
			.sentence(sentence)
			.sentenceMeaning(sentenceMeaning)
			.build();
	}

	public static VideoLearningExpressionQuizItem createExpressionQuizItem(
		String question,
		String original,
		List<String> choices,
		String meaning
	) {
		return VideoLearningExpressionQuizItem.builder()
			.question(question)
			.original(original)
			.choices(choices)
			.meaning(meaning)
			.build();
	}

	public static VideoLearningWordQuizListResponse createWordQuizListResponse(
		List<VideoLearningWordQuizItem> items
	) {
		return VideoLearningWordQuizListResponse.builder()
			.quiz(items)
			.build();
	}

	public static VideoLearningExpressionQuizListResponse createExpressionQuizListResponse(
		List<VideoLearningExpressionQuizItem> items
	) {
		return VideoLearningExpressionQuizListResponse.builder()
			.quiz(items)
			.build();
	}
}
