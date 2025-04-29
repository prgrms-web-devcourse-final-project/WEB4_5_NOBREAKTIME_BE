package com.mallang.mallang_backend.domain.video.youtube.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.video.youtube.client.YouTubeClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class YoutubeService {

	@Value("${youtube.api.key}")
	private String apiKey;

	// 검색: 키워드 기반으로 videoId만 가져오기
	public List<String> searchVideoIds(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long maxResults
	) throws IOException {
		YouTube youtubeService = YouTubeClient.getClient();

		YouTube.Search.List searchRequest = youtubeService.search()
			.list(List.of("id"));

		searchRequest.setQ(query);
		searchRequest.setType(List.of("video"));
		searchRequest.setVideoLicense("creativeCommon");
		searchRequest.setOrder("relevance");
		searchRequest.setRelevanceLanguage(relevanceLanguage);
		searchRequest.setRegionCode(regionCode);

		// 카테고리 필터 (존재할 경우)
		if (categoryId != null && !categoryId.isBlank()) {
			searchRequest.setVideoCategoryId(categoryId);
		}

		searchRequest.setMaxResults(maxResults);
		searchRequest.setVideoDuration("medium"); // 4~20분
		searchRequest.setKey(apiKey);

		SearchListResponse response = searchRequest.execute();
		return response.getItems().stream()
			.map(item -> item.getId().getVideoId())
			.collect(Collectors.toList());
	}

	// 상세조회: videoId 리스트로 Video 정보 가져오기
	public List<Video> fetchVideosByIds(List<String> videoIds) throws IOException {
		YouTube youtubeService = YouTubeClient.getClient();

		YouTube.Videos.List videosRequest = youtubeService.videos()
			.list(List.of("id", "snippet", "contentDetails", "status"));

		videosRequest.setId(videoIds);
		videosRequest.setKey(apiKey);

		VideoListResponse response = videosRequest.execute();
		return response.getItems();
	}

}
