package com.mallang.mallang_backend.domain.video.video.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.SearchContext;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoQueryService {

	private final VideoSearchProperties youtubeSearchProperties;
	private final YoutubeService youtubeService;

	/**
	 * YouTube API 호출 결과 전체 리스트를 캐시에 저장.
	 * 이후 동일 파라미터 요청 시 캐시에서 바로 반환.
	 */
	@Cacheable(
		value = "videoListCache",
		key = "T(String).format(\"%s|%s|%s|%d\", #q, #category, #language, #maxResults)"
	)
	public List<VideoResponse> queryVideos(
		String q,
		String category,
		String language,
		long maxResults
	) throws IOException {
		// 1) 검색 컨텍스트 빌드
		SearchContext ctx = buildSearchContext(q, category, language);

		// 2) YouTube Search API로 ID 목록 조회
		List<String> videoIds = youtubeService.searchVideoIds(
			ctx.getQuery(),
			ctx.getRegion(),
			ctx.getLangKey(),
			ctx.getCategory(),
			maxResults
		);
		if (videoIds.isEmpty()) {
			return List.of();
		}

		// 3) YouTube Videos API로 상세 정보 조회
		List<Video> ytVideos = youtubeService.fetchVideosByIds(videoIds);

		// 4) 필터링·매핑·셔플까지 한 번에 처리
		List<VideoResponse> responses = ytVideos.stream()
			.filter(VideoUtils::isCreativeCommons)
			.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
			.filter(VideoUtils::isDurationLessThanOrEqualTo20Minutes)
			.map(VideoUtils::toVideoResponse)
			.collect(Collectors.toList());

		// 기본 검색(default)일 때만 셔플
		return VideoUtils.shuffleIfDefault(responses, ctx.isDefaultSearch());
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
