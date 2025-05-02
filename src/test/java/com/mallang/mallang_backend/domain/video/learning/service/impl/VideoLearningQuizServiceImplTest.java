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
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
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

	@Mock
	private ExpressionRepository expressionRepository;

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

		// Subtitle/Keyword 준비 (id 필드는 빌더로 세팅 후 reflection 사용 가능)
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
		String videoId = "vid-001";
		when(expressionRepository.findAllByVideosId(videoId))
			.thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> quizService.makeExpressionQuizList(videoId))
			.isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPRESSION_NOT_FOUND);
	}

	@Test
	@DisplayName("표현 퀴즈 생성 - 정상 동작")
	void makeExpressionQuizList_withExpressions() {
		String videoId = "vid-001";
		Expression expr1 = mock(Expression.class);
		when(expr1.getSentence()).thenReturn("Let's learn expressions.");
		Expression expr2 = mock(Expression.class);
		when(expr2.getSentence()).thenReturn("This is a test expression.");

		when(expressionRepository.findAllByVideosId(videoId))
			.thenReturn(Arrays.asList(expr1, expr2));

		VideoLearningExpressionQuizListResponse response =
			quizService.makeExpressionQuizList(videoId);
		List<VideoLearningExpressionQuizItem> items = response.getQuiz();

		assertThat(items).hasSize(2);
		assertThat(items).extracting(VideoLearningExpressionQuizItem::getQuestion)
			.containsExactlyInAnyOrder(
				"Let's learn expressions.",
				"This is a test expression."
			);

		for (VideoLearningExpressionQuizItem item : items) {
			assertThat(item.getAnswer()).isEqualTo(item.getQuestion());

			List<String> choices = item.getChoices();
			if (item.getQuestion().equals("Let's learn expressions.")) {
				assertThat(choices)
					.containsExactlyInAnyOrder("Lets", "learn", "expressions");
			} else {
				assertThat(choices)
					.containsExactlyInAnyOrder("This", "is", "a", "test", "expression");
			}
		}
	}
}
