package com.mallang.mallang_backend.domain.video.youtube.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.video.youtube.client.YoutubeApiClient;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeService {

	private final YoutubeApiClient rawService;

	@Qualifier("youtubeApiExecutor")
	private final Executor youtubeApiExecutor;

	private static final String CACHE_NAME = "videoListCache";
	private final RedisCacheManager cacheManager;

	/**
	 * 검색: 키워드 기반으로 videoId만 가져오기
	 */
	@Retry(name = "videoSearch")
	@CircuitBreaker(name = "youtubeService")
	@Bulkhead(name = "youtubeService", type = Bulkhead.Type.SEMAPHORE)
	public List<String> searchVideoIds(
		String query,
		String regionCode,
		String relevanceLanguage,
		String categoryId,
		long desiredCount,
		String videoDuration
	) throws IOException {
		List<String> allVideoIds = new ArrayList<>();
		String nextPageToken = null;

		do {
			long max = Math.min(desiredCount - allVideoIds.size(), 50);
			// videoDuration 파라미터를 rawService.searchOnce() 에도 전달
			SearchListResponse resp = rawService.searchOnce(
				query, regionCode, relevanceLanguage,
				categoryId, nextPageToken, max, videoDuration
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
	 */
	@Retry(name = "videoSearch")
	@CircuitBreaker(name = "youtubeService")
	@Bulkhead(name = "youtubeService", type = Bulkhead.Type.SEMAPHORE)
	@TimeLimiter(name = "youtubeService")
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
				throw new ServiceException(ErrorCode.API_ERROR, e);
			}
		}, youtubeApiExecutor);
	}
}
