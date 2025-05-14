package com.mallang.mallang_backend.domain.video.video.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.video.dto.SearchContext;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * YouTube API로부터 동영상 목록을 가져오고,
 * 필터링 및 매핑 후 결과를 캐시에 저장하여 재사용하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCacheService {

	private final YoutubeService youtubeService;
	private final VideoSearchProperties youtubeSearchProperties;
	private final CacheManager cacheManager;
	private final RedisDistributedLock redisDistributedLock;

	// 순환 참조 방지를 위한 자기 자신 프록시
	@Autowired @Lazy
	private VideoCacheService self;

	/**
	 * 캐시에서 조회하고 MISS 시 fetchAndCache 호출
	 */
	@Cacheable(
		cacheNames = "videoListCache",
		key = "T(String).format(\"%s|%s|%s\", #q, #category, #language)"
	)
	public CachedVideos loadCached(
		String q, String category, String language, long fetchSize
	) {
		log.info("[CACHE MISS] loadCached q={} category={} language={}", q, category, language);
		return fetchAndCache(q, category, language, fetchSize);
	}

	/**
	 * YouTube API 호출 및 결과 필터링 후 캐시에 저장
	 */
	@CachePut(
		cacheNames = "videoListCache",
		key = "T(String).format(\"%s|%s|%s\", #q, #category, #language)"
	)
	public CachedVideos fetchAndCache(
		String q, String category, String language, long fetchSize
	) {
		try {
			log.info("[CACHE PUT] fetchAndCache q={} category={} language={} fetchSize={}",
				q, category, language, fetchSize);

			SearchContext ctx = buildSearchContext(q, category, language);
			List<String> ids = youtubeService.searchVideoIds(
				ctx.getQuery(), ctx.getRegion(), ctx.getLangKey(),
				ctx.getCategory(), fetchSize
			);

			List<VideoResponse> list = ids.isEmpty()
				? List.of()
				: youtubeService.fetchVideosByIds(ids).stream()
				.filter(VideoUtils::isCreativeCommons)
				.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
				.filter(v -> VideoUtils.isDurationLessThanOrEqualTo20Minutes(v))
				.map(VideoUtils::toVideoResponse)
				.collect(Collectors.toList());

			return new CachedVideos(fetchSize, list);

		} catch (IOException e) {
			log.error("[ERROR] fetchAndCache failed q={} category={} language={} fetchSize={}",
				q, category, language, fetchSize, e);
			throw new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED, e);
		}
	}

	/**
	 * 전체 영상 리스트 반환 (락 + 캐시 stampede 방지)
	 */
	public List<VideoResponse> getFullVideoList(
		String q, String category, String language, long fetchSize
	) {
		String key = String.format("%s|%s|%s", q, category, language);
		Cache cache = cacheManager.getCache("videoListCache");
		boolean hit = cache != null && cache.get(key) != null;
		log.info(hit ? "[CACHE HIT] key={}" : "[CACHE MISS] key={}", key);

		String lockKey   = "lock:video:cache:" + key;
		String lockValue = UUID.randomUUID().toString();

		// 락 획득 시도 (TTL: 30초)
		boolean lockAcquired = redisDistributedLock.tryLock(lockKey, lockValue, LOCK_TTL_MS);
		if (lockAcquired) {
			log.info("[CACHE LOCK ACQUIRED] key={} value={}", lockKey, lockValue);
		} else {
			log.info("[CACHE LOCK WAIT] key={}, waiting up to {}ms", lockKey, LOCK_TTL_MS);
			// 최대 30초 동안 100ms 간격으로 락 해제 여부 확인
			boolean waited = redisDistributedLock.waitForUnlockThenFetch(lockKey, LOCK_TTL_MS, WAIT_INTERVAL_MS);
			if (waited) {
				log.info("[CACHE LOCK RELEASED] key={}, proceeding", lockKey);
			} else {
				log.warn("[CACHE LOCK TIMEOUT] key={} after waiting {}ms", key, LOCK_TTL_MS);
			}
		}

		try {
			// 실제 캐시 조회 및 필요 시 업데이트
			CachedVideos cached = self.loadCached(q, category, language, fetchSize);
			if (cached.getRawFetchSize() < fetchSize) {
				log.info("[CACHE UPDATE] storedSize={} < requestedSize={} → refreshing cache",
					cached.getRawFetchSize(), fetchSize);
				cached = self.fetchAndCache(q, category, language, fetchSize);
			} else {
				log.info("[CACHE OK] storedSize={} ≥ requestedSize={}",
					cached.getRawFetchSize(), fetchSize);
			}

			List<VideoResponse> all = cached.getResponses();
			return all.size() <= fetchSize
				? all
				: all.subList(0, (int) fetchSize);

		} finally {
			// 락 해제 (획득한 경우에만)
			if (lockAcquired) {
				boolean released = redisDistributedLock.unlock(lockKey, lockValue);
				if (released) {
					log.info("[CACHE LOCK RELEASED] key={} value={}", lockKey, lockValue);
				} else {
					log.error("[CACHE LOCK RELEASE FAILED] key={} value={}", lockKey, lockValue);
				}
			}
		}
	}

	/**
	 * 검색 컨텍스트 생성 (쿼리, 지역, 언어 등)
	 */
	private SearchContext buildSearchContext(
		String q, String category, String language
	) {
		String langKey = (language != null && !language.isBlank())
			? language.toLowerCase()
			: "en";

		var defaultsMap = youtubeSearchProperties.getDefaults();
		var defaults    = defaultsMap.getOrDefault(langKey, defaultsMap.get("en"));

		String region = defaults.getRegion();
		String query  = (q != null && !q.isBlank()) ? q : defaults.getQuery();
		boolean isDefault = (q == null || q.isBlank()) && (category == null || category.isBlank());

		return new SearchContext(query, region, langKey, category, isDefault);
	}
}
