package com.mallang.mallang_backend.domain.video.video.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.video.video.cache.service.VideoCacheService;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;

import lombok.RequiredArgsConstructor;

/**
 * 동영상 목록을 캐시에서 조회하고, 매번 셔플하여 반환하는 서비스
 */
@Service
@RequiredArgsConstructor
public class VideoQueryService {
	private final VideoCacheService cacheService;

	/**
	 * 캐시에서 전체 필터링된 VideoResponse 리스트를 조회한 뒤,
	 * 매 호출마다 리스트를 랜덤으로 섞고(maxResults 개수로 제한하여) 반환합니다.
	 *
	 * @param q          검색어
	 * @param category   카테고리
	 * @param language   언어 코드
	 * @param maxResults 반환할 최대 동영상 수
	 * @return 요청한 개수만큼 셔플된 VideoResponse 리스트
	 */
	@TimeTrace
	public List<VideoResponse> queryVideos(
		String q,
		String category,
		String language,
		long maxResults
	) {
		// 캐시에서 전체 리스트(최대 fetchSize만큼) 가져오기
		List<VideoResponse> full = cacheService.getFullVideoList(q, category, language, maxResults);

		// 리스트 복사 및 랜덤 셔플
		List<VideoResponse> shuffled = new ArrayList<>(full);
		Collections.shuffle(shuffled);

		// 요청 개수(maxResults) 만큼 잘라서 반환
		// 전체 개수가 요청 개수 이하이면 전체 리스트를 반환,
		// 그렇지 않으면 maxResults 개수만큼 subList 처리
		return shuffled.size() <= maxResults
			? shuffled
			: shuffled.subList(0, (int) maxResults);
	}
}
