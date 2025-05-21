package com.mallang.mallang_backend.domain.video.youtube.client;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.global.aop.monitor.MonitorExternalApi;
import com.mallang.mallang_backend.global.dto.TokenUsageType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class YoutubeApiClient {

	@Value("${youtube.api.key}")
	private String apiKey;

	private final YouTubeClient youtubeClient;

	// 검색: 키워드 기반으로 videoId만 가져오기 (한 페이지 분량)
	@MonitorExternalApi(name = "youtube")
	public SearchListResponse searchOnce(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		String pageToken,
		long maxResults,
		String videoDuration
	) throws IOException {
		YouTube.Search.List req = youtubeClient.getClient().search()
			.list(List.of("id"))
			.setQ(query)
			.setType(List.of("video"))
			.setVideoLicense("creativeCommon")
			.setOrder("relevance")
			.setRelevanceLanguage(relevanceLanguage)
			.setRegionCode(regionCode)
			.setMaxResults(maxResults)
			.setVideoDuration(videoDuration)
			.setKey(apiKey);

		if (categoryId != null && !categoryId.isBlank()) {
			req.setVideoCategoryId(categoryId);
		}
		if (pageToken != null) {
			req.setPageToken(pageToken);
		}
		return req.execute();
	}

	/**
	 * 실제 동기 fetch 로직 (청크 단위 상세조회)
	 */
	@MonitorExternalApi(name = "youtube", usageType = TokenUsageType.FETCH_REQUEST)
	public VideoListResponse fetchOnce(List<String> ids) throws IOException {
		return youtubeClient.getClient().videos()
			.list(List.of("id", "snippet", "contentDetails", "status"))
			.setId(ids)
			.setKey(apiKey)
			.execute();
	}
}
