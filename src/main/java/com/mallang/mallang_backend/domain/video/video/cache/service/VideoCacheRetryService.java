package com.mallang.mallang_backend.domain.video.video.cache.service;

import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoCacheRetryService {
	private final CacheManager cacheManager;

	private static final String CACHE_NAME = "videoListCache";

	/**
	 * videoListCache에서 CachedVideos 가져옴
	 */
	@Retry(name = "videoCache", fallbackMethod = "fallbackGetCachedVideos")
	public CachedVideos getCachedVideos(String key) {
		Cache cache = cacheManager.getCache(CACHE_NAME);
		if (cache == null) {
			throw new ServiceException(ErrorCode.API_ERROR);
		}
		CachedVideos cv = cache.get(key, CachedVideos.class);
		if (cv == null) {
			throw new ServiceException(ErrorCode.API_ERROR);
		}
		return cv;
	}

	/**
	 * 재시도 전부 실패했을 때 호출
	 */
	private CachedVideos fallbackGetCachedVideos(String key, Throwable t) {
		log.error("[videoCacheRetry] 캐시 조회 실패 key={}", key, t);
		return new CachedVideos(0, List.of());
	}
}
