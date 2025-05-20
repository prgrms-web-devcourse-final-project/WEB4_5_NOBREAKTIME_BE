package com.mallang.mallang_backend.domain.video.youtube.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.video.youtube.client.YoutubeApiClient;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class YoutubeServiceTest {

	@Mock
	private YoutubeApiClient rawService;

	private final Executor directExecutor = Runnable::run;

	private YoutubeService service;

	@BeforeEach
	void setUp() {
		service = new YoutubeService(rawService, directExecutor);
	}

	private SearchListResponse makeSearchResp(List<String> ids, String nextPageToken) {
		List<SearchResult> items = new ArrayList<>();
		for (String id : ids) {
			ResourceId rid = new ResourceId().setVideoId(id);
			items.add(new SearchResult().setId(rid));
		}
		return new SearchListResponse()
			.setItems(items)
			.setNextPageToken(nextPageToken);
	}

	// helper: VideoListResponse 만들기
	private VideoListResponse makeVideoListResp(List<Video> videos) {
		return new VideoListResponse().setItems(videos);
	}

	@Test
	@DisplayName("searchVideoIds: 단일 페이지에서 ID 조회 성공")
	void searchVideoIds_singlePage() throws IOException {
		when(rawService.searchOnce("q", "US", "en", "10", null, 3L))
			.thenReturn(makeSearchResp(List.of("a", "b"), null));

		List<String> result = service.searchVideoIds("q","US","en","10", 3L);

		assertEquals(List.of("a","b"), result);
		verify(rawService).searchOnce("q","US","en","10", null, 3L);
	}

	@Test
	@DisplayName("searchVideoIds: 여러 페이지에서 원하는 개수만큼 조회 성공")
	void searchVideoIds_multiPage() throws IOException {
		when(rawService.searchOnce("q","US","en","10", null, 3L))
			.thenReturn(makeSearchResp(List.of("a","b"), "tok"));
		when(rawService.searchOnce("q","US","en","10", "tok", 1L))
			.thenReturn(makeSearchResp(List.of("c","d"), null));

		List<String> result = service.searchVideoIds("q","US","en","10", 3L);

		assertEquals(List.of("a","b","c"), result);
		verify(rawService, times(2)).searchOnce(anyString(), anyString(), anyString(), anyString(), any(), anyLong());
	}

	@Test
	@DisplayName("searchVideoIds: IOException 발생 시 그대로 던져짐")
	void searchVideoIds_ioExceptionPropagates() throws IOException {
		when(rawService.searchOnce(anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
			.thenThrow(new IOException("network error"));

		assertThrows(IOException.class, () ->
			service.searchVideoIds("q","US","en","10", 1L)
		);
	}

	@Test
	@DisplayName("fetchVideosByIdsAsync: 정상적으로 Video 리스트 반환")
	void fetchVideosByIdsAsync_success() throws Exception {
		List<String> ids = List.of("x","y");
		Video v1 = new Video().setId("x");
		Video v2 = new Video().setId("y");

		when(rawService.fetchOnce(ids))
			.thenReturn(makeVideoListResp(List.of(v1, v2)));

		CompletableFuture<List<Video>> future = service.fetchVideosByIdsAsync(ids);
		List<Video> videos = future.get(1, TimeUnit.SECONDS);

		assertEquals(2, videos.size());
		assertTrue(videos.contains(v1));
		assertTrue(videos.contains(v2));
	}

	@Test
	@DisplayName("fetchVideosByIdsAsync: IOException 시 ServiceException으로 변환")
	void fetchVideosByIdsAsync_ioExceptionBecomesServiceException() throws IOException {
		List<String> ids = List.of("x","y");
		when(rawService.fetchOnce(anyList()))
			.thenThrow(new IOException("detail fetch failed"));

		CompletableFuture<List<Video>> future = service.fetchVideosByIdsAsync(ids);

		ExecutionException ex = assertThrows(ExecutionException.class, () ->
			future.get(1, TimeUnit.SECONDS)
		);
		assertTrue(ex.getCause() instanceof ServiceException);
		assertEquals(ErrorCode.API_ERROR, ((ServiceException)ex.getCause()).getErrorCode());
	}

	@Test
	@DisplayName("fallbackSearchVideoIds: ServiceException 발생")
	void fallbackSearchVideoIds_throwsServiceException() {
		ServiceException ex = assertThrows(ServiceException.class, () ->
			service.fallbackSearchVideoIds("q","US","en","10", 5L, new Throwable())
		);
		assertEquals(ErrorCode.API_ERROR, ex.getErrorCode());
	}

	@Test
	@DisplayName("fallbackFetchVideosByIds: CompletableFuture가 예외 상태로 완료됨")
	void fallbackFetchVideosByIds_completesExceptionally() {
		CompletableFuture<List<Video>> future =
			service.fallbackFetchVideosByIds(List.of("1","2"), new Throwable());

		assertTrue(future.isCompletedExceptionally());
		ExecutionException ex = assertThrows(ExecutionException.class, future::get);
		assertTrue(ex.getCause() instanceof ServiceException);
		assertEquals(ErrorCode.API_ERROR, ((ServiceException)ex.getCause()).getErrorCode());
	}
}
