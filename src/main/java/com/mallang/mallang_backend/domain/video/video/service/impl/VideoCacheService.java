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
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

/**
 * YouTube API로부터 동영상 목록을 가져오고,
 * 필터링 및 매핑 후 결과를 캐시에 저장하여 재사용
 */
@Service
@RequiredArgsConstructor
public class VideoCacheService {
	private final YoutubeService youtubeService;
	private final VideoSearchProperties youtubeSearchProperties;

	/**
	 * 전체 필터링된 동영상 리스트를 캐시에서 조회, 없으면 YoutubeService를 통해 가져와 캐시에 저장
	 * @param q
	 * @param category
	 * @param language
	 * @param fetchSize
	 * @return
	 */
	@Cacheable(
		cacheNames = "videoListCache",
		key = "T(String).format(\"%s|%s|%s\", #q, #category, #language)"
	)
	public List<VideoResponse> getFullVideoList(
		String q, String category, String language, long fetchSize
	) {
		try {
			//  SearchContext 생성, query, region, langKey 등 설정
			SearchContext ctx = buildSearchContext(q, category, language);

			// YouTube API에서 ID 목록 조회
			List<String> ids = youtubeService.searchVideoIds(
				ctx.getQuery(), ctx.getRegion(), ctx.getLangKey(),
				ctx.getCategory(), fetchSize
			);
			if (ids.isEmpty()) return List.of();

			// 상세 정보 조회 후 Creative Commons 필터, 언어 필터, 길이 필터
			return youtubeService.fetchVideosByIds(ids).stream()
				.filter(VideoUtils::isCreativeCommons)
				.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
				.filter(v -> VideoUtils.isDurationLessThanOrEqualTo20Minutes(v))
				.map(VideoUtils::toVideoResponse)
				.collect(Collectors.toList());

		} catch (IOException e) {
			// 전역 예외로 래핑
			throw new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED, e);
		}
	}

	/**
	 * 검색 파라미터를 기반으로 SearchContext를 생성
	 * @param q
	 * @param category
	 * @param language
	 * @return
	 */
	private SearchContext buildSearchContext(
		String q, String category, String language
	) {
		String langKey = (language != null && !language.isBlank())
			? language.toLowerCase()
			: "en";

		// 언어별 기본 설정 조회
		var defaults = youtubeSearchProperties.getDefaults()
			.getOrDefault(langKey, youtubeSearchProperties.getDefaults().get("en"));

		String region = defaults.getRegion();
		String query = (q != null && !q.isBlank()) ? q : defaults.getQuery();
		boolean isDefault = (q == null || q.isBlank()) && (category == null || category.isBlank());

		return new SearchContext(query, region, langKey, category, isDefault);
	}
}