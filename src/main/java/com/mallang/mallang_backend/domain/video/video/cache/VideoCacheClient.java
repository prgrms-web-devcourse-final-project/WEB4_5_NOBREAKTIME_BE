package com.mallang.mallang_backend.domain.video.video.cache;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.video.dto.SearchContext;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoCacheClient {
	private final YoutubeService youtubeService;
	private final VideoSearchProperties youtubeSearchProperties;

	/**
	 * 캐시에서 조회하고 MISS 시 fetchAndCache 호출
	 */
	@Cacheable(
		cacheNames = "videoListCache",
		key = "T(String).format(" +
			"  '%s|%s|%s', " +
			"  (#q        == null ? '' : #q), " +
			"  (#category == null ? '' : #category), " +
			"  #language" +
			")",
		unless = "#result != null && #result.responses.isEmpty()"
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
		key = "T(String).format(" +
			"  '%s|%s|%s', " +
			"  (#q        == null ? '' : #q), " +
			"  (#category == null ? '' : #category), " +
			"  #language" +
			")"
	)
	public CachedVideos fetchAndCache(
		String q, String category, String language, long fetchSize
	) {
		String safeQ       = (q == null)        ? "" : q;
		String safeCategory= (category == null) ? "" : category;

		try {
			log.info("[CACHE PUT] fetchAndCache q={} category={} language={} fetchSize={}",
				safeQ, safeCategory, language, fetchSize);

			SearchContext ctx = buildSearchContext(q, category, language);
			List<String> ids = youtubeService.searchVideoIds(
				ctx.getQuery(),
				ctx.getRegion(),
				ctx.getLangKey(),
				ctx.getCategory(),
				fetchSize,
				ctx.getVideoDuration()
			);

			List<VideoResponse> list = ids.isEmpty()
				? List.of()
				: youtubeService.fetchVideosByIdsAsync(ids)
				.join().stream()
				.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
				.map(VideoUtils::toVideoResponse)
				.collect(Collectors.toList());

			return new CachedVideos(fetchSize, list);

		} catch (IOException e) {
			log.error("[ERROR] fetchAndCache failed q={} category={} language={} fetchSize={}",
				safeQ, safeCategory, language, fetchSize, e);
			throw new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED, e);
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

		String region        = defaults.getRegion();
		String query         = (q != null && !q.isBlank())      ? q             : defaults.getQuery();
		boolean isDefault    = (q == null || q.isBlank()) && (category == null || category.isBlank());
		String videoDuration = defaults.getVideoDuration();

		return new SearchContext(query, region, langKey, category, isDefault, videoDuration);
	}
}
