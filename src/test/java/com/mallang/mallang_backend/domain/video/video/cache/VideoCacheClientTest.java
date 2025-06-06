package com.mallang.mallang_backend.domain.video.video.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.cache.client.VideoCacheClient;
import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.YoutubeSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.config.YoutubeSearchProperties.SearchDefault;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class VideoCacheClientTest {

	@Mock
	private YoutubeService youtubeService;

	@Mock
	private YoutubeSearchProperties youtubeSearchProperties;

	private VideoCacheClient client;
	private SearchDefault enDefault;

	@BeforeEach
	void setUp() {
		// 기본 defaults 맵 설정
		enDefault = new SearchDefault();
		enDefault.setRegion("US");
		enDefault.setQuery("defaultQuery");
		enDefault.setVideoDuration("medium");

		Map<String, SearchDefault> defaultsMap = Map.of("en", enDefault);
		when(youtubeSearchProperties.getDefaults()).thenReturn(defaultsMap);

		client = new VideoCacheClient(youtubeService, youtubeSearchProperties);
	}

	@Test
	@DisplayName("buildSearchContext: q, category, language 모두 null 이면 defaults 사용")
	void buildSearchContext_defaults() throws IOException {
		// searchVideoIds 호출 스텁: 마지막 videoDuration 은 anyString()
		when(youtubeService.searchVideoIds(
			anyString(), anyString(), anyString(), any(), anyLong(), anyString()
		)).thenReturn(Collections.emptyList());

		// category, q, language 모두 null
		client.fetchAndCache(null, null, null, 1L);

		// 기본 defaults 에서 query="defaultQuery", region="US", langKey="en", videoDuration="medium" 이 넘어가야 함
		verify(youtubeService).searchVideoIds(
			eq("defaultQuery"), // query
			eq("US"),           // region
			eq("en"),           // langKey
			isNull(),           // category
			eq(1L),             // fetchSize
			eq("medium")        // videoDuration
		);
	}

	@Test
	@DisplayName("fetchAndCache: 검색된 ID 없으면 빈 responses 반환")
	void fetchAndCache_emptyIds() throws IOException {
		when(youtubeService.searchVideoIds(
			eq("foo"), eq("US"), eq("en"), isNull(), eq(10L), eq("medium")
		)).thenReturn(Collections.emptyList());

		CachedVideos result = client.fetchAndCache("foo", null, "en", 10L);

		assertEquals(10L, result.getRawFetchSize());
		assertTrue(result.getResponses().isEmpty());
		verify(youtubeService, never()).fetchVideosByIdsAsync(anyList());
	}

	@Test
	@DisplayName("fetchAndCache: IOException 발생 시 ServiceException")
	void fetchAndCache_ioException() throws IOException {
		// 모든 파라미터 매칭 → IOException 던지기
		when(youtubeService.searchVideoIds(
			anyString(),    // q
			anyString(),    // region
			anyString(),    // langKey
			any(),          // category
			anyLong(),      // fetchSize
			any()           // videoDuration (nullable)
		)).thenThrow(new IOException("fail"));

		ServiceException ex = assertThrows(ServiceException.class, () ->
			client.fetchAndCache("q", "cat", "en", 5L)
		);
		assertEquals(ErrorCode.VIDEO_ID_SEARCH_FAILED, ex.getErrorCode());
	}

	@Test
	@DisplayName("fetchAndCache: IDs 가 있으면 VideoUtils 필터 후 toVideoResponse 변환")
	void fetchAndCache_withResults() throws IOException {
		List<String> ids = List.of("i1", "i2");
		// 1) searchVideoIds stub
		when(youtubeService.searchVideoIds(
			eq("bar"), eq("US"), eq("en"), eq("music"), eq(2L), eq("medium")
		)).thenReturn(ids);

		// 2) 원본 Video 객체 준비 (contentDetails 주입)
		Video rawVideo = new Video();
		VideoContentDetails details = new VideoContentDetails();
		details.setDuration("PT10M");           // 10분 길이
		rawVideo.setContentDetails(details);

		// 3) fetchVideosByIdsAsync stub
		CompletableFuture<List<Video>> rawFuture =
			CompletableFuture.completedFuture(List.of(rawVideo));
		when(youtubeService.fetchVideosByIdsAsync(ids)).thenReturn(rawFuture);

		// 4) VideoUtils static 메서드 stub
		VideoResponse mapped = Mockito.mock(VideoResponse.class);
		try (MockedStatic<VideoUtils> vs = mockStatic(VideoUtils.class)) {
			vs.when(() -> VideoUtils.matchesLanguage(rawVideo, "en")).thenReturn(true);
			vs.when(() -> VideoUtils.toVideoResponse(rawVideo)).thenReturn(mapped);

			// 실제 호출
			CachedVideos result = client.fetchAndCache("bar", "music", "en", 2L);

			// 검증
			assertEquals(2L, result.getRawFetchSize());
			assertEquals(1, result.getResponses().size());
			assertSame(mapped, result.getResponses().get(0));
		}
	}
}
