package com.mallang.mallang_backend.domain.video.video.cache.quartz.service;

import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.video.video.cache.client.VideoCacheClient;
import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.global.slack.SlackNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Quartz 스케줄러에서 호출되어
 * 지정된 q, category, language, fetchSize 로 YouTube API 요청을 강제 수행하고
 * 캐시를 갱신하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheSchedulerService {

	private final VideoCacheClient cacheClient;

	/**
	 * 캐시를 갱신합니다.
	 *
	 * @param q 검색어 (빈 문자열 또는 null 이면 기본 쿼리 사용)
	 * @param category 카테고리 ID (빈 문자열 또는 null 이면 기본 카테고리 사용)
	 * @param language ISO 언어 코드 (예: "en", "ko")
	 * @param fetchSize 가져올 최대 동영상 수
	 * @return 갱신된 캐시 항목 개수
	 */
	@SlackNotification(title = "캐시 갱신", message = "현재 캐시 갱신 스케줄링이 실행 준비 중입니다.")
	public int refreshCache(
		String q,
		String category,
		String language,
		long fetchSize
	) {
		long start = System.currentTimeMillis();

		CachedVideos updated = cacheClient.fetchAndCache(q, category, language, fetchSize);

		int count = updated.getResponses().size();
		log.info("캐시 갱신 완료: 검색어='{}', 카테고리='{}', 언어='{}', 요청수={} → 갱신된 항목 {}개 (실행시간={}ms)",
			q, category, language, fetchSize, count, System.currentTimeMillis() - start);
		return count;
	}
}
