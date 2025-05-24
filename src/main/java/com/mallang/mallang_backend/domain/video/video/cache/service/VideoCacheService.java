package com.mallang.mallang_backend.domain.video.video.cache.service;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.video.video.cache.VideoCacheClient;
import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCacheService {
	private final VideoCacheClient cacheClient;
	private final CacheManager cacheManager;
	private final RedisDistributedLock redisDistributedLock;

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
			boolean waited = redisDistributedLock.waitForUnlockThenFetch(lockKey, LOCK_TTL_MS, WAIT_INTERVAL_MS);
			if (waited) {
				log.info("[CACHE LOCK RELEASED] key={}, proceeding", lockKey);
			} else {
				log.error("[CACHE LOCK TIMEOUT] key={} after waiting {}ms – aborting cache flow", key, LOCK_TTL_MS);
				throw new ServiceException(ErrorCode.CACHE_LOCK_TIMEOUT);
			}
		}

		try {
			// 실제 캐시 조회 및 필요 시 업데이트
			CachedVideos cached = cacheClient.loadCached(q, category, language, fetchSize);
			if (cached.getRawFetchSize() < fetchSize) {
				log.info("[CACHE UPDATE] storedSize={} < requestedSize={} → refreshing cache",
					cached.getRawFetchSize(), fetchSize);
				cached = cacheClient.fetchAndCache(q, category, language, fetchSize);
			} else {
				log.info("[CACHE OK] storedSize={} ≥ requestedSize={}",
					cached.getRawFetchSize(), fetchSize);
			}

			List<VideoResponse> all = cached.getResponses();
			return all.size() <= fetchSize
				? all
				: all.subList(0, (int) fetchSize);

		} finally {
			// 락 해제 (획득한 경우에만) + 재시도 로직
			if (lockAcquired) {
				boolean released = redisDistributedLock.unlock(lockKey, lockValue);
				if (!released) {
					try { Thread.sleep(50); } catch (InterruptedException ignored) { }
					released = redisDistributedLock.unlock(lockKey, lockValue);
				}
				if (released) {
					log.info("[CACHE LOCK RELEASED] key={} value={}", lockKey, lockValue);
				} else {
					log.error("[CACHE LOCK RELEASE FAILED] key={} value={}", lockKey, lockValue);
				}
			}
		}
	}
}
