package com.mallang.mallang_backend.domain.video.learning.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizItem;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class VideoLearningQuizServiceImplTest {

	@Mock
	private KeywordRepository keywordRepository;

	@InjectMocks
	private VideoLearningQuizServiceImpl quizService;

	@Test
	@DisplayName("키워드가 없으면 ServiceException(KEYWORD_NOT_FOUND) 발생")
	void makeQuizList_noKeywords() {
		String videoId = "vid-001";
		when(keywordRepository.findAllByVideosId(videoId))
			.thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> quizService.makeQuizList(videoId))
			.isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.KEYWORD_NOT_FOUND);
	}

	@Test
	@DisplayName("키워드가 있으면 최대 개수만큼 퀴즈 항목 반환")
	void makeQuizList_withKeywords() {
		String videoId = "vid-001";
		// 두 개의 subtitle/keyword를 생성
		var sub1 = Subtitle.builder()
			.videos(null)
			.startTime("00:00:01")
			.endTime("00:00:02")
			.originalSentence("Hello world")
			.translatedSentence("안녕 세상")
			.speaker("Speaker1")
			.build();
		var sub2 = Subtitle.builder()
			.videos(null)
			.startTime("00:00:03")
			.endTime("00:00:04")
			.originalSentence("Test code")
			.translatedSentence("테스트 코드")
			.speaker("Speaker2")
			.build();
		Keyword kw1 = Keyword.builder().videos(null).subtitle(sub1)
			.word("Hello").meaning("안녕").difficulty(Difficulty.EASY).build();
		Keyword kw2 = Keyword.builder().videos(null).subtitle(sub2)
			.word("Test").meaning("테스트").difficulty(Difficulty.NORMAL).build();
		List<Keyword> keywords = Arrays.asList(kw1, kw2);
		when(keywordRepository.findAllByVideosId(videoId))
			.thenReturn(keywords);

		VideoLearningWordQuizListResponse response = quizService.makeQuizList(videoId);

		List<VideoLearningWordQuizItem> items = response.getQuiz();
		assertThat(items).hasSize(2);
		assertThat(items).extracting(VideoLearningWordQuizItem::getWord)
			.containsExactlyInAnyOrder("Hello", "Test");
	}
}
