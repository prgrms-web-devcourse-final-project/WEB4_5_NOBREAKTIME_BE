package com.mallang.mallang_backend.domain.video.youtube.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.video.youtube.client.YouTubeClient;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class YoutubeService {

	@Value("${youtube.api.key}")
	private String apiKey;

	// 검색: 키워드 기반으로 videoId만 가져오기
	@Retryable(retryFor = IOException.class, interceptor = "retryOperationsInterceptor")
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
	@Retryable(retryFor = IOException.class, interceptor = "retryOperationsInterceptor")
	public List<Video> fetchVideosByIds(List<String> videoIds) throws IOException {
		YouTube youtubeService = YouTubeClient.getClient();

		YouTube.Videos.List videosRequest = youtubeService.videos()
			.list(List.of("id", "snippet", "contentDetails", "status"));

		videosRequest.setId(videoIds);
		videosRequest.setKey(apiKey);

		VideoListResponse response = videosRequest.execute();
		return response.getItems();
	}



	/** searchVideoIds() 최대 재시도 후에도 IOException을 던지면 호출 */
	@Recover
	public List<String> recoverSearchVideoIds(
		IOException ex,
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long maxResults
	) {
		throw new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED);
	}

	/** fetchVideosByIds() 최대 재시도 후에도 IOException을 던지면 호출*/
	@Recover
	public List<Video> recoverFetchVideosByIds(
		IOException ex,
		List<String> videoIds
	) {
		throw new ServiceException(ErrorCode.VIDEO_DETAIL_FETCH_FAILED);
	}

}
