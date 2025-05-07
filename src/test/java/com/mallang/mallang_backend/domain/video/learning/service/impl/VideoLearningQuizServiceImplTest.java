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
import org.springframework.test.util.ReflectionTestUtils;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizItem;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
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
	@DisplayName("단어 퀴즈: 키워드가 없으면 ServiceException(KEYWORD_NOT_FOUND) 발생")
	void makeQuizList_noKeywords() {
		String videoId = "vid-001";
		when(keywordRepository.findAllByVideosId(videoId))
			.thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> quizService.makeQuizList(videoId))
			.isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.KEYWORD_NOT_FOUND);
	}

	@Test
	@DisplayName("단어 퀴즈: 키워드가 있으면 최대 개수만큼 퀴즈 항목 반환")
	void makeQuizList_withKeywords() {
		String videoId = "vid-001";

		Subtitle sub1 = Subtitle.builder()
			.videos(null)
			.startTime("00:00:01")
			.endTime("00:00:02")
			.originalSentence("Hello world")
			.translatedSentence("안녕 세상")
			.speaker("Speaker1")
			.build();
		Subtitle sub2 = Subtitle.builder()
			.videos(null)
			.startTime("00:00:03")
			.endTime("00:00:04")
			.originalSentence("Test code")
			.translatedSentence("테스트 코드")
			.speaker("Speaker2")
			.build();
		ReflectionTestUtils.setField(sub1, "id", 1L);
		ReflectionTestUtils.setField(sub2, "id", 2L);

		Keyword kw1 = Keyword.builder()
			.videos(null)
			.subtitle(sub1)
			.word("Hello")
			.meaning("안녕")
			.difficulty(Difficulty.EASY)
			.build();
		Keyword kw2 = Keyword.builder()
			.videos(null)
			.subtitle(sub2)
			.word("Test")
			.meaning("테스트")
			.difficulty(Difficulty.NORMAL)
			.build();

		when(keywordRepository.findAllByVideosId(videoId))
			.thenReturn(Arrays.asList(kw1, kw2));

		VideoLearningWordQuizListResponse response = quizService.makeQuizList(videoId);
		List<VideoLearningWordQuizItem> items = response.getQuiz();

		assertThat(items).hasSize(2);
		assertThat(items).extracting(VideoLearningWordQuizItem::getWord)
			.containsExactlyInAnyOrder("Hello", "Test");
	}

	@Test
	@DisplayName("표현 퀴즈 빈 리스트 예외(EXPRESSION_NOT_FOUND)")
	void makeExpressionQuizList_emptyPool() {
		String videoId = "vid-002";
		when(keywordRepository.findAllByVideosId(videoId))
			.thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> quizService.makeExpressionQuizList(videoId))
			.isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPRESSION_NOT_FOUND);
	}

	@Test
	@DisplayName("표현 퀴즈 생성 - 정상 동작")
	void makeExpressionQuizList_withKeywords() {
		String videoId = "vid-003";

		Subtitle sub1 = Subtitle.builder()
			.videos(null)
			.startTime("00:00:01")
			.endTime("00:00:05")
			.originalSentence("Let's learn expressions, shall we?")
			.translatedSentence("표현을 배워봅시다, 그럴까요?")
			.speaker("Narrator")
			.build();
		Subtitle sub2 = Subtitle.builder()
			.videos(null)
			.startTime("00:00:06")
			.endTime("00:00:10")
			.originalSentence("This is a test expression; it is important.")
			.translatedSentence("이것은 테스트 표현입니다; 중요합니다.")
			.speaker("Narrator")
			.build();
		ReflectionTestUtils.setField(sub1, "id", 1L);
		ReflectionTestUtils.setField(sub2, "id", 2L);

		Keyword kw1 = Keyword.builder()
			.videos(null)
			.subtitle(sub1)
			.word("expressions")
			.meaning("표현")
			.difficulty(Difficulty.EASY)
			.build();
		Keyword kw2 = Keyword.builder()
			.videos(null)
			.subtitle(sub2)
			.word("important")
			.meaning("중요한")
			.difficulty(Difficulty.EASY)
			.build();

		when(keywordRepository.findAllByVideosId(videoId))
			.thenReturn(Arrays.asList(kw1, kw2));

		VideoLearningExpressionQuizListResponse response = quizService.makeExpressionQuizList(videoId);
		List<VideoLearningExpressionQuizItem> items = response.getQuiz();

		assertThat(items).hasSize(2);

		for (VideoLearningExpressionQuizItem item : items) {
			String original = item.getOriginal();
			if ("Let's learn expressions, shall we?".equals(original)) {
				assertThat(item.getQuestion()).isEqualTo("{} {} {}, {} {}?");
				assertThat(item.getChoices())
					.containsExactlyInAnyOrder("Lets", "learn", "expressions", "shall", "we");
				assertThat(item.getMeaning()).isEqualTo("표현을 배워봅시다, 그럴까요?");
			} else if ("This is a test expression; it is important.".equals(original)) {
				assertThat(item.getQuestion()).isEqualTo("{} {} {} {} {}; {} {} {}.");
				assertThat(item.getChoices())
					.containsExactlyInAnyOrder("This", "is", "a", "test", "expression", "it", "is", "important");
				assertThat(item.getMeaning()).isEqualTo("이것은 테스트 표현입니다; 중요합니다.");
			} else {
				fail("Unexpected original: " + original);
			}
		}
	}
}
