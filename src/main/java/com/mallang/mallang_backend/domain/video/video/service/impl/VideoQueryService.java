package com.mallang.mallang_backend.domain.video.video.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoQueryService {
	private final VideoCacheService cacheService;

	public List<VideoResponse> queryVideos(
		String q, String category, String language, long maxResults
	) throws IOException {
		// 캐시에서 전체 리스트(최대 fetchSize만큼) 꺼내고
		List<VideoResponse> full = cacheService.getFullVideoList(q, category, language, maxResults);
		// 매번 셔플 + subList
		List<VideoResponse> shuffled = new ArrayList<>(full);
		Collections.shuffle(shuffled);
		return shuffled.size() <= maxResults
			? shuffled
			: shuffled.subList(0, (int) maxResults);
	}
}