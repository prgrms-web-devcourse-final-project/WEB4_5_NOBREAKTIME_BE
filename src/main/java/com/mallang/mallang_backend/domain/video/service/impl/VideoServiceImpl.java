package com.mallang.mallang_backend.domain.video.service.impl;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.service.VideoService;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoFilterUtils;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

	private final VideoSearchProperties youtubeSearchProperties;
	private final YoutubeService youtubeService;

	@Override
	public List<VideoResponse> getVideosByLanguage(
		String q,
		String category,
		String language,
		long maxResults
	) {
		// 1) 언어 키 및 기본 설정
		String langKey = Optional.ofNullable(language).orElse("en").toLowerCase();
		var defaults = youtubeSearchProperties.getDefaults()
			.getOrDefault(langKey, youtubeSearchProperties.getDefaults().get("en"));
		String regionCode   = defaults.getRegion();
		String defaultQuery = defaults.getQuery();

		// 2) 쿼리 > 카테고리 > 기본 검색어
		String effectiveQuery = (q != null && !q.isBlank()) ? q
			: (category != null && !category.isBlank() ? category : defaultQuery);

		// 3) ID 목록 조회 using YoutubeService
		List<String> videoIds;
		try {
			videoIds = youtubeService.searchVideoIds(effectiveQuery, regionCode, langKey, maxResults);
		} catch (IOException e) {
			throw new RuntimeException("Failed to search video IDs", e);
		}
		if (videoIds == null || videoIds.isEmpty()) {
			return Collections.emptyList();
		}

		// 4) 상세 비디오 정보 조회
		List<Video> videoList;
		try {
			videoList = youtubeService.fetchVideosByIds(videoIds);
		} catch (IOException e) {
			throw new RuntimeException("Failed to fetch video details", e);
		}

		// 5) 최종 필터 및 DTO 매핑 (null-safe)
		return Optional.ofNullable(videoList).orElse(Collections.emptyList()).stream()
			.filter(v -> v.getStatus() != null && "creativeCommon".equals(v.getStatus().getLicense()))
			.filter(v -> {
				var snip = v.getSnippet();
				return snip != null && langKey.equals(snip.getDefaultAudioLanguage());
			})
			.filter(VideoFilterUtils::isDurationLessThanOrEqualTo20Minutes)
			.map(v -> new VideoResponse(
				v.getId(),
				v.getSnippet().getTitle(),
				v.getSnippet().getDescription(),
				v.getSnippet().getThumbnails().getMedium().getUrl()
			))
			.collect(Collectors.toList());
	}
}

// @Override
    // public List<VideoResponse> getVideosByLanguage(String language, long maxResults) {
    //     List<VideoResponse> results = new ArrayList<>();
    //
    //     try {
    //         String langKey = language.toLowerCase();
    //
    //         // 언어에 맞는 기본 검색어와 지역코드를 가져온다
    //         VideoSearchProperties.SearchDefault searchDefault = youtubeSearchProperties.getDefaults().get(langKey);

    // String searchQuery = (query == null || query.isBlank())
    //     ? searchDefault.getQuery()
    //     : query;


    //
    //         // 검색
    //         List<String> videoIds = youtubeService.searchVideoIds(
    //             searchDefault.getQuery(),
    //             searchDefault.getRegion(),
    //             langKey,
    //             maxResults
    //         );
    //
    //         // 상세 조회
    //         List<Video> videoList = youtubeService.fetchVideosByIds(videoIds);
    //
    //         for (Video video : videoList) {
    //             if (VideoFilterUtils.isDurationLessThanOrEqualTo20Minutes(video)) {
    //                 results.add(new VideoResponse(
    //                     video.getId(),
    //                     video.getSnippet().getTitle(),
    //                     video.getSnippet().getDescription(),
    //                     video.getSnippet().getThumbnails().getMedium().getUrl()
    //                 ));
    //             }
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //
    //     return results;
    // }
