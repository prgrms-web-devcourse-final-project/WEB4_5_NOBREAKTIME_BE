package com.mallang.mallang_backend.domain.video.youtube.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Service
@RequiredArgsConstructor
public class YoutubeService {

	@Value("${youtube.api.key}")
	private String apiKey;

	private final YouTubeClient youtubeClient;

	@Qualifier("youtubeApiExecutor")
	private final Executor youtubeApiExecutor;

	// 검색: 키워드 기반으로 videoId만 가져오기
	@Retry(name = "apiRetry", fallbackMethod = "fallbackSearchVideoIds")
	@CircuitBreaker(name = "youtubeService", fallbackMethod = "fallbackSearchVideoIds")
	public List<String> searchVideoIds(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long desiredCount
	) throws IOException {
		List<String> allVideoIds = new ArrayList<>();
		String nextPageToken = null;

		// YouTube API는 페이지당 최대 50개 반환
		// 페이지네이션을 통해 desiredCount만큼 누적
		do {
			// Search List 요청 빌드
			YouTube.Search.List request = youtubeClient.getClient().search()
				.list(List.of("id"))
				.setQ(query)
				.setType(List.of("video"))
				.setVideoLicense("creativeCommon")
				.setOrder("relevance")
				.setRelevanceLanguage(relevanceLanguage)
				.setRegionCode(regionCode)
				// 남은 개수 vs 50 중 작은 값으로 요청
				.setMaxResults(Math.min(desiredCount - allVideoIds.size(), 50))
				.setKey(apiKey);

			// 카테고리 필터 (존재할 경우)
			if (categoryId != null && !categoryId.isBlank()) {
				request.setVideoCategoryId(categoryId);
			}
			// 다음 페이지 토큰이 존재하면 설정
			if (nextPageToken != null) {
				request.setPageToken(nextPageToken);
			}

			// 요청 실행 및 결과 누적
			SearchListResponse response = request.execute();
			response.getItems().stream()
				.map(item -> item.getId().getVideoId())
				.forEach(allVideoIds::add);

			// 다음 페이지 토큰 설정
			nextPageToken = response.getNextPageToken();
		} while (nextPageToken != null && allVideoIds.size() < desiredCount);

		// 실제로 모아진 개수만큼 리턴
		return allVideoIds.stream()
			.limit(desiredCount)
			.collect(Collectors.toList());
	}

	// 상세조회: videoId 리스트로 Video 정보 가져오기
	@Retry(name = "apiRetry", fallbackMethod = "fallbackFetchVideosByIds")
	@CircuitBreaker(name = "youtubeService", fallbackMethod = "fallbackFetchVideosByIds")
	public List<Video> fetchVideosByIds(List<String> videoIds) {
		YouTube youtube = youtubeClient.getClient();

		// 50개씩 분할
		List<List<String>> chunks = new ArrayList<>();
		for (int i = 0; i < videoIds.size(); i += 50) {
			int end = Math.min(i + 50, videoIds.size());
			chunks.add(videoIds.subList(i, end));
		}

		// 비동기 호출 Futures 생성
		List<CompletableFuture<List<Video>>> futures = chunks.stream()
			.map(chunk -> CompletableFuture.supplyAsync(() -> {
				try {
					VideoListResponse resp = youtube.videos()
						.list(List.of("id", "snippet", "contentDetails", "status"))
						.setId(chunk)
						.setKey(apiKey)
						.execute();
					return resp.getItems();
				} catch (IOException ex) {
					throw new ServiceException(ErrorCode.API_ERROR);
				}
			}, youtubeApiExecutor))
			.toList();

		// 모든 Future가 끝날 때까지 대기 후 합치기
		return futures.stream()
			.map(CompletableFuture::join)
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	/** searchVideoIds() 최대 재시도 후에도 IOException을 던지면 호출 */
	public List<String> fallbackSearchVideoIds(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long desiredCount,
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
