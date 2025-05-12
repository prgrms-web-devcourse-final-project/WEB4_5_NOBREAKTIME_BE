package com.mallang.mallang_backend.domain.video.video.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import com.mallang.mallang_backend.global.exception.ErrorCode;
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
	public AnalyzeVideoResponse fetchAnalyzedResultAfterWait(String videoId) {
		List<Subtitle> savedSubtitles = subtitleRepository.findAllByVideosFetchKeywords(videoId);
		if (savedSubtitles.isEmpty()) {
			throw new ServiceException(ErrorCode.ANALYZE_VIDEO_FAILED);
		}
		List<GptSubtitleResponse> subtitleResponses = GptSubtitleResponse.from(savedSubtitles);
		return AnalyzeVideoResponse.from(subtitleResponses);
	}
}
