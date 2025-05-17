package com.mallang.mallang_backend.domain.plan.entity.domain.video.learning.service.impl;

import com.mallang.mallang_backend.domain.plan.entity.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.plan.entity.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.learning.dto.VideoLearningExpressionQuizItem;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.learning.dto.VideoLearningWordQuizItem;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
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
	private final ExpressionRepository expressionRepository;
	private final Random random = new Random();

	/**
	 * 주어진 videoId에 해당하는 빈칸 퀴즈 목록을 생성해서 반환. 현재는 랜덤 추출이라 추후 수정 필요
	 * @param videoId
	 * @return VideoLearningQuizListResponse
	 */
	@Override
	@Transactional(readOnly = true)
	public VideoLearningWordQuizListResponse makeQuizList(String videoId) {
		// 영상 id로 연관 키워드 조회
		List<Keyword> pool = keywordRepository.findAllByVideosId(videoId);
		if (pool.isEmpty()) {
			throw new ServiceException(ErrorCode.KEYWORD_NOT_FOUND);
		}

		// 자막 id로 그룹핑
		Map<Long, List<Keyword>> bySubtitle = pool.stream()
			.collect(Collectors.groupingBy(k -> k.getSubtitles().getId()));

		// 각 그룹에서 랜덤 추출 리스트 변환
		List<Keyword> picked = bySubtitle.values().stream()
			.map(list -> {
				Collections.shuffle(list, random);
				return list.get(0);
			})
			.collect(Collectors.toList());

		// 뽑힌 항목 전체 셔플 후 최대 갯수만큼 제한
		Collections.shuffle(picked, random);

		List<VideoLearningWordQuizItem> items = picked.stream()
			.map(VideoLearningWordQuizItem::from)
			.collect(Collectors.toList());

		return VideoLearningWordQuizListResponse.builder()
			.quiz(items)
			.build();
	}

	/**
	 * 주어진 videoId에 해당하는 표현 퀴즈 목록을 생성해서 반환
	 * @param videoId
	 * @return VideoLearningExpressionQuizListResponse
	 */
	@Override
	@Transactional(readOnly = true)
	public VideoLearningExpressionQuizListResponse makeExpressionQuizList(String videoId) {
		List<Keyword> pool = keywordRepository.findAllByVideosId(videoId);
		if (pool.isEmpty()) throw new ServiceException(ErrorCode.EXPRESSION_NOT_FOUND);

		// Subtitle별 그룹
		Map<Subtitle, List<Keyword>> bySubtitle = pool.stream()
			.collect(Collectors.groupingBy(Keyword::getSubtitles));

		// QuizItem 생성
		List<VideoLearningExpressionQuizItem> items = bySubtitle.keySet().stream()
			.map(subtitle ->
				VideoLearningExpressionQuizItem.fromSubtitle(subtitle, random)
			)
			.collect(Collectors.toList());

		return VideoLearningExpressionQuizListResponse.builder()
			.quiz(items)
			.build();
	}
}
