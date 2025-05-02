package com.mallang.mallang_backend.domain.video.learning.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizItem;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoLearningQuizServiceImpl implements VideoLearningQuizService {

	private final KeywordRepository keywordRepository;
	private final Random random = new Random();

	/**
	 * 주어진 videoId에 해당하는 빈칸 퀴즈 목록을 생성해서 반환. 현재는 랜덤 추출이라 추후 수정 필요
	 * @param videoId
	 * @return VideoLearningQuizListResponse
	 */
	@Override
	@Transactional(readOnly = true)
	public VideoLearningWordQuizListResponse makeQuizList(String videoId) {
		List<Keyword> pool = keywordRepository.findAllByVideosId(videoId);
		if (pool.isEmpty()) {
			throw new ServiceException(ErrorCode.KEYWORD_NOT_FOUND);
		}

		// subtitleId별로 묶기
		Map<Long, List<Keyword>> bySubtitle = pool.stream()
			.collect(Collectors.groupingBy(k -> k.getSubtitles().getId()));


		// 각 그룹에서 랜덤으로 하나씩 뽑기
		List<Keyword> picked = bySubtitle.values().stream()
			.map(list -> {
				Collections.shuffle(list, random);
				return list.get(0);
			})
			.collect(Collectors.toList());

		// 전체를 섞고, 최대 개수만큼 잘라내기
		Collections.shuffle(picked, random);
		List<VideoLearningWordQuizItem> items = picked.stream()
			.limit(MAX_VIDEO_LEARNING_QUIZ_ITEMS)
			.map(VideoLearningWordQuizItem::from)
			.collect(Collectors.toList());

		return VideoLearningWordQuizListResponse.builder()
			.quiz(items)
			.build();
	}
}
