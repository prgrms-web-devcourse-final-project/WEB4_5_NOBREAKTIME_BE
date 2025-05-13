package com.mallang.mallang_backend.domain.video.video.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.SearchContext;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoCacheService {
	private final YoutubeService youtubeService;
	private final VideoSearchProperties youtubeSearchProperties;


	@Cacheable(
		cacheNames = "videoListCache",
		key = "T(String).format(\"%s|%s|%s\", #q, #category, #language)"
	)
	public List<VideoResponse> getFullVideoList(
		String q, String category, String language, long fetchSize
	) throws IOException {
		// fetchSize 만큼 ids 뽑고 필터링 → 반환
		SearchContext ctx = buildSearchContext(q, category, language);
		List<String> ids = youtubeService.searchVideoIds(
			ctx.getQuery(), ctx.getRegion(), ctx.getLangKey(),
			ctx.getCategory(), fetchSize
		);
		if (ids.isEmpty()) return List.of();
		return youtubeService.fetchVideosByIds(ids).stream()
			.filter(VideoUtils::isCreativeCommons)
			.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
			.filter(v -> VideoUtils.isDurationLessThanOrEqualTo20Minutes(v))
			.map(VideoUtils::toVideoResponse)
			.collect(Collectors.toList());
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
		String query = (q != null && !q.isBlank()) ? q : defaults.getQuery();
		boolean isDefault = (q == null || q.isBlank()) && (category == null || category.isBlank());

		return new SearchContext(query, region, langKey, category, isDefault);
	}
}