// ───── YoutubeService.java ─────
package com.mallang.mallang_backend.domain.plan.entity.domain.video.youtube.service;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.youtube.client.YoutubeApiClient;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeService {

	private final YoutubeApiClient rawService;

	// Youtube API 비동기 호출을 위한 Executor
	@Qualifier("youtubeApiExecutor")
	private final Executor youtubeApiExecutor;

	// 검색: 키워드 기반으로 videoId만 가져오기
	@Retry(name = "apiRetry", fallbackMethod = "fallbackSearchVideoIds")
	@CircuitBreaker(name = "youtubeService", fallbackMethod = "fallbackSearchVideoIds")
	@Bulkhead(name = "youtubeService", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "fallbackSearchVideoIds")
	public List<String> searchVideoIds(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long desiredCount
	) throws IOException {
		List<String> allVideoIds = new ArrayList<>();
		String nextPageToken = null;

		do {
			long max = Math.min(desiredCount - allVideoIds.size(), 50);
			SearchListResponse resp = rawService.searchOnce(
				query, regionCode, relevanceLanguage, categoryId, nextPageToken, max
			);
			resp.getItems().stream()
				.map(item -> item.getId().getVideoId())
				.forEach(allVideoIds::add);
			nextPageToken = resp.getNextPageToken();
		} while (nextPageToken != null && allVideoIds.size() < desiredCount);

		return allVideoIds.stream()
			.limit(desiredCount)
			.collect(Collectors.toList());
	}

	/**
	 * 상세조회: videoId 리스트로 Video 정보 가져오기 (비동기)
	 * - 50개씩 chunk로 분할하고, CompletableFuture로 병렬 호출
	 */
	@Retry(name = "apiRetry", fallbackMethod = "fallbackFetchVideosByIds")
	@CircuitBreaker(name = "youtubeService", fallbackMethod = "fallbackFetchVideosByIds")
	@Bulkhead(name = "youtubeService", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "fallbackFetchVideosByIds")
	@TimeLimiter(name = "youtubeService", fallbackMethod = "fallbackFetchVideosByIds")
	public CompletableFuture<List<Video>> fetchVideosByIdsAsync(List<String> videoIds) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				List<List<String>> chunks = new ArrayList<>();
				for (int i = 0; i < videoIds.size(); i += 50) {
					int end = Math.min(i + 50, videoIds.size());
					chunks.add(videoIds.subList(i, end));
				}

				List<Video> results = new ArrayList<>();
				for (List<String> c : chunks) {
					VideoListResponse resp = rawService.fetchOnce(c);
					results.addAll(resp.getItems());
				}
				return results;
			} catch (IOException e) {
				throw new ServiceException(ErrorCode.API_ERROR);
			}
		}, youtubeApiExecutor);
	}

	/**
	 * searchVideoIds() 최대 재시도 후에도 실패 시 호출
	 */
	public List<String> fallbackSearchVideoIds(
		String query, String regionCode, String relevanceLanguage, String categoryId,
		long desiredCount, Throwable t
	) {
		throw new ServiceException(ErrorCode.API_ERROR);
	}

	/**
	 * fetchVideosByIdsAsync() 최대 재시도 후에도 실패 시 호출
	 */
	public CompletableFuture<List<Video>> fallbackFetchVideosByIds(
		List<String> videoIds, Throwable t
	) {
		CompletableFuture<List<Video>> failed = new CompletableFuture<>();
		failed.completeExceptionally(new ServiceException(ErrorCode.API_ERROR));
		return failed;
	}
}
