package com.mallang.mallang_backend.domain.video.learning.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.testfactory.VideoLearningTestFactory;

@ExtendWith(MockitoExtension.class)
class VideoLearningQuizServiceImplTest {

	@Mock
	private KeywordRepository keywordRepository;

	@Mock
	private ExpressionRepository expressionRepository;

	private VideoLearningQuizServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new VideoLearningQuizServiceImpl(keywordRepository, expressionRepository);
		// 랜덤 시드를 고정하여 예측 가능한 결과 보장
		ReflectionTestUtils.setField(service, "random", new Random(0));
	}

	@Test
	@DisplayName("makeQuizList: 키워드 없으면 KEYWORD_NOT_FOUND 예외 발생")
	void makeQuizList_noKeywords_throws() {
		when(keywordRepository.findAllByVideosId("vid1"))
			.thenReturn(Collections.emptyList());

		ServiceException ex = assertThrows(
			ServiceException.class,
			() -> service.makeQuizList("vid1")
		);
		assertEquals(ErrorCode.KEYWORD_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	@DisplayName("makeQuizList: 같은 Subtitle 그룹이면 하나만 뽑힌다")
	void makeQuizList_groupingBySubtitle() {
		var sub = VideoLearningTestFactory.mockSubtitle(
			1L,
			"hello world example",
			"안녕 세계 예시",
			"00:00", "00:03", "S"
		);
		Keyword k1 = VideoLearningTestFactory.mockKeyword(sub, "hello", "안녕");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(sub, "world", "세계");

		when(keywordRepository.findAllByVideosId("vid2"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningWordQuizListResponse resp = service.makeQuizList("vid2");
		assertNotNull(resp.getQuiz());
		assertEquals(1, resp.getQuiz().size());
	}

	@Test
	@DisplayName("makeQuizList: 서로 다른 Subtitle 그룹별로 각각 하나씩 뽑힌다")
	void makeQuizList_multipleGroups() {
		var s1 = VideoLearningTestFactory.mockSubtitle(
			1L, "apple banana", "사과 바나나", "00:00", "00:05", "A"
		);
		var s2 = VideoLearningTestFactory.mockSubtitle(
			2L, "cat dog", "고양이 개", "00:06", "00:10", "B"
		);
		Keyword k1 = VideoLearningTestFactory.mockKeyword(s1, "apple", "사과");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(s2, "cat", "고양이");

		when(keywordRepository.findAllByVideosId("vid3"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningWordQuizListResponse resp = service.makeQuizList("vid3");
		assertNotNull(resp.getQuiz());
		assertEquals(2, resp.getQuiz().size());
	}

	@Test
	@DisplayName("makeExpressionQuizList: 키워드 없으면 EXPRESSION_NOT_FOUND 예외 발생")
	void makeExpressionQuizList_noKeywords_throws() {
		when(keywordRepository.findAllByVideosId("vid4"))
			.thenReturn(Collections.emptyList());

		ServiceException ex = assertThrows(
			ServiceException.class,
			() -> service.makeExpressionQuizList("vid4")
		);
		assertEquals(ErrorCode.EXPRESSION_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	@DisplayName("makeExpressionQuizList: 같은 Subtitle 그룹이면 하나만 생성")
	void makeExpressionQuizList_groupingBySubtitle() {
		var sub = VideoLearningTestFactory.mockSubtitle(
			1L, "quick brown fox", "빠른 갈색 여우", "00:00", "00:05", "N"
		);
		Keyword k1 = VideoLearningTestFactory.mockKeyword(sub, "quick", "빠른");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(sub, "brown", "갈색");

		when(keywordRepository.findAllByVideosId("vid5"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningExpressionQuizListResponse resp = service.makeExpressionQuizList("vid5");
		assertNotNull(resp.getQuiz());
		assertEquals(1, resp.getQuiz().size());
	}

	@Test
	@DisplayName("makeExpressionQuizList: 서로 다른 Subtitle 그룹별로 각각 하나씩 생성")
	void makeExpressionQuizList_multipleGroups() {
		var s1 = VideoLearningTestFactory.mockSubtitle(
			1L, "one two three", "하나 둘 셋", "00:00", "00:06", "X"
		);
		var s2 = VideoLearningTestFactory.mockSubtitle(
			2L, "red blue green", "빨강 파랑 초록", "00:07", "00:13", "Y"
		);
		Keyword k1 = VideoLearningTestFactory.mockKeyword(s1, "one", "하나");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(s2, "red", "빨강");

		when(keywordRepository.findAllByVideosId("vid6"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningExpressionQuizListResponse resp = service.makeExpressionQuizList("vid6");
		assertNotNull(resp.getQuiz());
		assertEquals(2, resp.getQuiz().size());
	}
}
