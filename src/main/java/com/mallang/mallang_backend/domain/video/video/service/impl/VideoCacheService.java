package com.mallang.mallang_backend.domain.video.video.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.video.video.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.SearchContext;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

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

	// 순환 참조 회피를 위한 자기 자신 프록시
	@Autowired @Lazy
	private VideoCacheService self;

	/**
	 * 전체 필터링된 동영상 리스트를 캐시에서 조회,
	 * 없으면 fetchAndCache를 통해 가져와 캐시에 저장
	 *
	 * @param q 검색어
	 * @param category 카테고리
	 * @param language 언어 코드
	 * @param fetchSize YouTube에서 조회할 최대 크기
	 * @return CachedVideos(rawFetchSize와 필터링된 VideoResponse 리스트)
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
	 * YouTube에서 동영상을 조회하여 캐시에 저장
	 *
	 * @param q 검색어
	 * @param category 카테고리
	 * @param language 언어 코드
	 * @param fetchSize 실제 조회할 크기
	 * @return CachedVideos(rawFetchSize와 필터링된 VideoResponse 리스트)
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
	 * 캐시에서 저장된 결과를 조회하고,
	 * 저장된 fetchSize가 요청 fetchSize보다 작으면 캐시를 갱신,
	 * 이후 요청 개수만큼 잘라서 반환
	 *
	 * @param q 검색어
	 * @param category 카테고리
	 * @param language 언어 코드
	 * @param fetchSize 요청 개수
	 * @return 요청한 개수만큼의 VideoResponse 리스트
	 */
	public List<VideoResponse> getFullVideoList(
		String q, String category, String language, long fetchSize
	) {
		String key = String.format("%s|%s|%s", q, category, language);
		Cache cache = cacheManager.getCache("videoListCache");
		if (cache != null && cache.get(key) != null) {
			log.info("[CACHE HIT] key={}", key);
		} else {
			log.info("[CACHE MISS] key={}", key);
		}

		CachedVideos cached = self.loadCached(q, category, language, fetchSize);
		if (cached.getRawFetchSize() < fetchSize) {
			log.info("[CACHE UPDATE] storedSize={} < requestedSize={}",
				cached.getRawFetchSize(), fetchSize);
			cached = self.fetchAndCache(q, category, language, fetchSize);
		} else {
			log.info("[CACHE OK] storedSize={} >= requestedSize={}",
				cached.getRawFetchSize(), fetchSize);
		}

		List<VideoResponse> all = cached.getResponses();
		return all.size() <= fetchSize
			? all
			: all.subList(0, (int) fetchSize);
	}

	/**
	 * 검색 파라미터(q, category, language)에 따라 SearchContext 생성
	 */
	private SearchContext buildSearchContext(
		String q, String category, String language
	) {
		String langKey = (language != null && !language.isBlank())
			? language.toLowerCase()
			: "en";

		var defaults = youtubeSearchProperties.getDefaults()
			.getOrDefault(langKey, youtubeSearchProperties.getDefaults().get("en"));

		String region = defaults.getRegion();
		String query = (q != null && !q.isBlank()) ? q : defaults.getQuery();
		boolean isDefault = (q == null || q.isBlank()) && (category == null || category.isBlank());

		return new SearchContext(query, region, langKey, category, isDefault);
	}
}