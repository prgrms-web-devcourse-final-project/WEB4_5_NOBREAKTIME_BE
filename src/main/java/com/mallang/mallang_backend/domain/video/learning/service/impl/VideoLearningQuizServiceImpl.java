package com.mallang.mallang_backend.domain.video.learning.service.impl;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizItem;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizItem;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoLearningQuizServiceImpl implements VideoLearningQuizService {

	private final KeywordRepository keywordRepository;
	private final Random random = new Random();

	/**
	 * 영상 단어 퀴즈 생성
	 */
	@Override
	@Cacheable(value = "wordQuizCache", key = "#videoId", sync = true)
	@Transactional(readOnly = true)
	public VideoLearningWordQuizListResponse makeQuizList(String videoId) {
		List<Keyword> pool = keywordRepository.findAllByVideosId(videoId);
		if (pool.isEmpty()) {
			throw new ServiceException(ErrorCode.KEYWORD_NOT_FOUND);
		}

		// 자막 ID별로 그룹핑 후 각 그룹에서 랜덤 하나씩 뽑기
		Map<Long, List<Keyword>> bySubtitle = pool.stream()
			.collect(Collectors.groupingBy(k -> k.getSubtitles().getId()));

		List<Keyword> picked = bySubtitle.values().stream()
			.map(list -> {
				Collections.shuffle(list, random);
				return list.get(0);
			})
			.collect(Collectors.toList());

		Collections.shuffle(picked, random);

		// VideoLearningWordQuizItem 변환
		List<VideoLearningWordQuizItem> items = picked.stream()
			.map(VideoLearningWordQuizItem::from)
			.collect(Collectors.toList());

		return VideoLearningWordQuizListResponse.builder()
			.quiz(items)
			.build();
	}

	/**
	 * 영상 표현 퀴즈 생성 (영어/일본어 분기)
	 */
	@Override
	@Cacheable(value = "expressionQuizCache", key = "#videoId", sync = true)
	@Transactional(readOnly = true)
	public VideoLearningExpressionQuizListResponse makeExpressionQuizList(String videoId) {
		List<Keyword> pool = keywordRepository.findAllByVideosId(videoId);
		if (pool.isEmpty()) {
			throw new ServiceException(ErrorCode.EXPRESSION_NOT_FOUND);
		}

		// 첫 키워드의 자막을 통해 영상 언어 판별
		Subtitle firstSub = pool.get(0).getSubtitles();
		Language lang = (firstSub.getVideos() != null)
			? firstSub.getVideos().getLanguage()
			: Language.ENGLISH;

		// Subtitle별로 그룹핑
		Map<Subtitle, List<Keyword>> bySubtitle = pool.stream()
			.collect(Collectors.groupingBy(Keyword::getSubtitles));

		// 언어별로 각 Subtitle → QuizItem 변환
		List<VideoLearningExpressionQuizItem> items;
		if (lang == Language.JAPANESE) {
			// 일본어 분기
			items = bySubtitle.keySet().stream()
				.map(sub -> VideoLearningExpressionQuizItem.fromSubtitleJapanese(sub, random))
				.collect(Collectors.toList());
		} else {
			// 영어 포함 그 외 언어 분기
			items = bySubtitle.keySet().stream()
				.map(sub -> VideoLearningExpressionQuizItem.fromSubtitleEnglish(sub, random))
				.collect(Collectors.toList());
		}

		return VideoLearningExpressionQuizListResponse.builder()
			.quiz(items)
			.build();
	}
}
