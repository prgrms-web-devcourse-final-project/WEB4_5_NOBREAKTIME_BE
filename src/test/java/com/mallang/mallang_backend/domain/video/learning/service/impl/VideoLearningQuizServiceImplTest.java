package com.mallang.mallang_backend.domain.video.learning.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Constructor;
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
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.testfactory.VideoLearningTestFactory;

@ExtendWith(MockitoExtension.class)
class VideoLearningQuizServiceImplTest {

	@Mock
	private KeywordRepository keywordRepository;

	private VideoLearningQuizServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new VideoLearningQuizServiceImpl(keywordRepository);
		ReflectionTestUtils.setField(service, "random", new Random(0));
	}

	/** protected 생성자를 우회해 Videos 인스턴스 생성 후 subtitle에 세팅 */
	private Subtitle attachVideos(Subtitle sub, Language lang) {
		try {
			Constructor<Videos> ctor = Videos.class.getDeclaredConstructor();
			ctor.setAccessible(true);
			Videos videos = ctor.newInstance();
			ReflectionTestUtils.setField(videos, "language", lang);
			ReflectionTestUtils.setField(sub, "videos", videos);
			return sub;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		Subtitle sub = VideoLearningTestFactory.mockSubtitle(
			1L,
			"hello world example",
			"안녕 세계 예시",
			"00:00", "00:03", "S"
		);
		attachVideos(sub, Language.ENGLISH);

		Keyword k1 = VideoLearningTestFactory.mockKeyword(sub, "hello", "안녕");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(sub, "world", "세계");
		when(keywordRepository.findAllByVideosId("vid2"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningWordQuizListResponse resp = service.makeQuizList("vid2");
		assertEquals(1, resp.getQuiz().size());
	}

	@Test
	@DisplayName("makeQuizList: 서로 다른 Subtitle 그룹별로 각각 하나씩 뽑힌다")
	void makeQuizList_multipleGroups() {
		Subtitle s1 = VideoLearningTestFactory.mockSubtitle(
			1L, "apple banana", "사과 바나나", "00:00", "00:05", "A"
		);
		Subtitle s2 = VideoLearningTestFactory.mockSubtitle(
			2L, "cat dog", "고양이 개", "00:06", "00:10", "B"
		);
		attachVideos(s1, Language.ENGLISH);
		attachVideos(s2, Language.ENGLISH);

		Keyword k1 = VideoLearningTestFactory.mockKeyword(s1, "apple", "사과");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(s2, "cat", "고양이");
		when(keywordRepository.findAllByVideosId("vid3"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningWordQuizListResponse resp = service.makeQuizList("vid3");
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
	@DisplayName("makeExpressionQuizList: 같은 Subtitle 그룹이면 하나만 생성 (영어)")
	void makeExpressionQuizList_groupingBySubtitle_english() {
		Subtitle sub = VideoLearningTestFactory.mockSubtitle(
			1L, "quick brown fox", "빠른 갈색 여우", "00:00", "00:05", "N"
		);
		attachVideos(sub, Language.ENGLISH);

		Keyword k1 = VideoLearningTestFactory.mockKeyword(sub, "quick", "빠른");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(sub, "brown", "갈색");
		when(keywordRepository.findAllByVideosId("vid5"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningExpressionQuizListResponse resp = service.makeExpressionQuizList("vid5");
		assertEquals(1, resp.getQuiz().size());
	}

	@Test
	@DisplayName("makeExpressionQuizList: 서로 다른 Subtitle 그룹별로 각각 하나씩 생성 (영어)")
	void makeExpressionQuizList_multipleGroups_english() {
		Subtitle s1 = VideoLearningTestFactory.mockSubtitle(
			1L, "one two three", "하나 둘 셋", "00:00", "00:06", "X"
		);
		Subtitle s2 = VideoLearningTestFactory.mockSubtitle(
			2L, "red blue green", "빨강 파랑 초록", "00:07", "00:13", "Y"
		);
		attachVideos(s1, Language.ENGLISH);    // <— 반드시 호출!
		attachVideos(s2, Language.ENGLISH);    // <— 반드시 호출!

		Keyword k1 = VideoLearningTestFactory.mockKeyword(s1, "one", "하나");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(s2, "red", "빨강");
		when(keywordRepository.findAllByVideosId("vid6"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningExpressionQuizListResponse resp = service.makeExpressionQuizList("vid6");
		assertEquals(2, resp.getQuiz().size());
	}

	@Test
	@DisplayName("makeExpressionQuizList: 일본어 분기에서도 같은 Subtitle 그룹이면 하나만 생성")
	void makeExpressionQuizList_groupingBySubtitle_japanese() {
		Subtitle sub = VideoLearningTestFactory.mockSubtitle(
			1L, "ただその話にが何か言外の別の意味が含まれてる。", "단어 설명", "00:00", "00:05", "J"
		);
		attachVideos(sub, Language.JAPANESE);

		Keyword k1 = VideoLearningTestFactory.mockKeyword(sub, "話に", "이야기에");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(sub, "別の", "다른");
		when(keywordRepository.findAllByVideosId("vid7"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningExpressionQuizListResponse resp = service.makeExpressionQuizList("vid7");
		assertEquals(1, resp.getQuiz().size());
	}

	@Test
	@DisplayName("makeExpressionQuizList: 일본어 분기에서 여러 Subtitle 그룹별로 각각 하나씩 생성")
	void makeExpressionQuizList_multipleGroups_japanese() {
		Subtitle s1 = VideoLearningTestFactory.mockSubtitle(
			1L, "こんにちは世界", "안녕 세계", "00:00", "00:03", "J1"
		);
		Subtitle s2 = VideoLearningTestFactory.mockSubtitle(
			2L, "おはようございます", "좋은 아침입니다", "00:04", "00:08", "J2"
		);
		attachVideos(s1, Language.JAPANESE);
		attachVideos(s2, Language.JAPANESE);

		Keyword k1 = VideoLearningTestFactory.mockKeyword(s1, "こんにちは", "안녕");
		Keyword k2 = VideoLearningTestFactory.mockKeyword(s2, "おはようございます", "안녕하세요");
		when(keywordRepository.findAllByVideosId("vid8"))
			.thenReturn(Arrays.asList(k1, k2));

		VideoLearningExpressionQuizListResponse resp = service.makeExpressionQuizList("vid8");
		assertEquals(2, resp.getQuiz().size());
	}
}
