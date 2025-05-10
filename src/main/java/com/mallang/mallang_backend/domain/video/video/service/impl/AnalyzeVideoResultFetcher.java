package com.mallang.mallang_backend.domain.video.video.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.ANALYZE_VIDEO_FAILED;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.global.aop.TimeTrace;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;

import lombok.RequiredArgsConstructor;

/**
 * 영상 분석 시 같은 시점에 분석된 영상을 조회하기 위한 서비스
 */
@Service
@RequiredArgsConstructor
public class AnalyzeVideoResultFetcher {

	private final SubtitleRepository subtitleRepository;

	@TimeTrace
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public Optional<AnalyzeVideoResponse> fetchAnalyzedResultAfterWait(String videoId) {
		List<Subtitle> savedSubtitles = subtitleRepository.findAllByVideosFetchKeywords(videoId);
		if (savedSubtitles.isEmpty()) {
			return Optional.empty();
		}
		List<GptSubtitleResponse> subtitleResponses = GptSubtitleResponse.from(savedSubtitles);
		return Optional.ofNullable(AnalyzeVideoResponse.from(subtitleResponses));
	}
}
