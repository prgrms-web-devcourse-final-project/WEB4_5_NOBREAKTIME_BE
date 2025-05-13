package com.mallang.mallang_backend.domain.video.video.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.SearchContext;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.service.cache.VideoCacheEntry;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoQueryService {

	private final YoutubeService youtubeService;
	private final CacheManager cacheManager;
	private final VideoSearchProperties youtubeSearchProperties;
	private static final String CACHE_NAME = "videoListCache";

	public List<VideoResponse> queryVideos(
		String q,
		String category,
		String language,
		long maxResults
	) throws IOException {
		String key = String.format("%s|%s|%s", q, category, language);
		Cache cache = cacheManager.getCache(CACHE_NAME);

		// 1) 캐시에서 VideoCacheEntry 꺼내기
		VideoCacheEntry entry = cache.get(key, VideoCacheEntry.class);

		// 2) 캐시가 없거나, 이전에 가져온 양(fetchedMaxResults)보다
		//    더 많은 데이터를 요청(maxResults)이면 → 새로 조회 & 갱신
		if (entry == null || entry.getFetchedMaxResults() < maxResults) {
			List<VideoResponse> allList =
				fetchAndFilterAll(q, category, language, maxResults);

			entry = new VideoCacheEntry(allList, maxResults);
			cache.put(key, entry);
		}

		// 1) 원본 full list
		List<VideoResponse> fullList = entry.getVideos();

		// 2) 복사해서 섞기
		List<VideoResponse> shuffled = new ArrayList<>(fullList);
		Collections.shuffle(shuffled);

		// 3) 원하는 개수만큼 자르기
		if (shuffled.size() <= maxResults) {
			return shuffled;
		}
		return shuffled.subList(0, (int) maxResults);
	}

	private List<VideoResponse> fetchAndFilterAll(
		String q,
		String category,
		String language,
		long maxResults
	) throws IOException {
		SearchContext ctx = buildSearchContext(q, category, language);

		// YouTube Search → 상세 조회 → 필터 → 매핑 → (기본 검색 시) 셔플
		List<String> ids = youtubeService.searchVideoIds(
			ctx.getQuery(), ctx.getRegion(), ctx.getLangKey(),
			ctx.getCategory(), maxResults
		);
		if (ids.isEmpty()) return List.of();

		List<Video> ytVideos = youtubeService.fetchVideosByIds(ids);
		List<VideoResponse> list = ytVideos.stream()
			.filter(VideoUtils::isCreativeCommons)
			.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
			.filter(VideoUtils::isDurationLessThanOrEqualTo20Minutes)
			.map(VideoUtils::toVideoResponse)
			.collect(Collectors.toList());

		return VideoUtils.shuffleIfDefault(list, ctx.isDefaultSearch());
	}

	private SearchContext buildSearchContext(
		String q, String category, String language
	) {
		String langKey = (language != null && !language.isBlank())
			? language.toLowerCase()
			: "en";

		var defaults = youtubeSearchProperties.getDefaults()
			.getOrDefault(langKey, youtubeSearchProperties.getDefaults().get("en"));

		String region = defaults.getRegion();
		String query = (q != null && !q.isBlank())
			? q
			: defaults.getQuery();
		boolean isDefault = (q == null || q.isBlank())
			&& (category == null || category.isBlank());

		return new SearchContext(query, region, langKey, category, isDefault);
	}
}
