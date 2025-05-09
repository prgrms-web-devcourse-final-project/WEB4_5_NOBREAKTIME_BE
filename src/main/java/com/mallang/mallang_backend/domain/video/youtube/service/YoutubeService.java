package com.mallang.mallang_backend.domain.video.youtube.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.video.youtube.client.YouTubeClient;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeService {

	@Value("${youtube.api.key}")
	private String apiKey;

	// 검색: 키워드 기반으로 videoId만 가져오기
	@Retry(name = "apiRetry", fallbackMethod = "fallbackSearchVideoIds")
	@CircuitBreaker(name = "youtubeService", fallbackMethod = "fallbackSearchVideoIds")
	public List<String> searchVideoIds(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long maxResults
	) throws IOException {
		YouTube youtubeService = YouTubeClient.getClient();

		// Search List 요청 빌드
		YouTube.Search.List request = youtubeService.search()
			.list(List.of("id"))
			.setQ(query)
			.setType(List.of("video"))
			.setVideoLicense("creativeCommon")
			.setOrder("relevance")
			.setRelevanceLanguage(relevanceLanguage)
			.setRegionCode(regionCode);

		// 카테고리 필터 (존재할 경우)
		if (categoryId != null && !categoryId.isBlank()) {
			request.setVideoCategoryId(categoryId);
		}

		// 최대 결과 수와 영상 길이, API 키 설정
		request
			.setMaxResults(maxResults)
			.setVideoDuration("medium") // 4~20분
			.setKey(apiKey);

		// 실제 호출 및 응답 반환
		SearchListResponse response = request.execute();
		return response.getItems().stream()
			.map(item -> item.getId().getVideoId())
			.collect(Collectors.toList());
	}

	// 상세조회: videoId 리스트로 Video 정보 가져오기
	@Retry(name = "apiRetry", fallbackMethod = "fallbackFetchVideosByIds")
	@CircuitBreaker(name = "youtubeService", fallbackMethod = "fallbackSearchVideoIds")
	public List<Video> fetchVideosByIds(List<String> videoIds) throws IOException {
		YouTube youtubeService = YouTubeClient.getClient();

		VideoListResponse response = youtubeService.videos()
			.list(List.of("id", "snippet", "contentDetails", "status"))
			.setId(videoIds)
			.setKey(apiKey)
			.execute();

		return response.getItems();
	}

	/** searchVideoIds() 최대 재시도 후에도 IOException을 던지면 호출 */
	public List<String> fallbackSearchVideoIds(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long maxResults,
		Throwable t
	) {
		throw new ServiceException(ErrorCode.API_ERROR);
	}

	/** fetchVideosByIds() 최대 재시도 후에도 IOException을 던지면 호출 */
	public List<Video> fallbackFetchVideosByIds(
		List<String> videoIds,
		Throwable t
	) {
		throw new ServiceException(ErrorCode.API_ERROR);
	}
}
