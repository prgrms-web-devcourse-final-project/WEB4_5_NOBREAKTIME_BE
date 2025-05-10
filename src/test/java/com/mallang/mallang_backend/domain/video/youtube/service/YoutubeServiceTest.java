package com.mallang.mallang_backend.domain.video.youtube.service;

import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.video.youtube.client.YouTubeClient;

import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

@SpringBootTest
@TestPropertySource(properties = {
	// Retry
	"resilience4j.retry.instances.youtubeService.max-attempts=3",
	"resilience4j.retry.instances.youtubeService.wait-duration=0ms",
	// Bulkhead: 최대 2개 동시, 대기 없음
	"resilience4j.bulkhead.instances.youtubeService.max-concurrent-calls=2",
	"resilience4j.bulkhead.instances.youtubeService.max-wait-duration=0ms",
	// TimeLimiter: 100ms 타임아웃
	"resilience4j.timeLimiter.instances.youtubeService.timeout-duration=100ms",
})
class YoutubeServiceTest {

	@Autowired
	private YoutubeService youtubeService;

	@Autowired
	private TimeLimiterRegistry timeLimiterRegistry;

	private MockedStatic<YouTubeClient> youTubeClientStatic;
	private YouTube youtubeMock;
	private YouTube.Search searchMock;
	private YouTube.Search.List listMock;
	private YouTube.Videos videosMock;
	private YouTube.Videos.List videoListMock;

	@BeforeEach
	void setup() throws IOException {
		// YouTubeClient.getClient() 모킹
		youtubeMock   = mock(YouTube.class);
		searchMock    = mock(YouTube.Search.class);
		listMock      = mock(YouTube.Search.List.class);
		videosMock    = mock(YouTube.Videos.class);
		videoListMock = mock(YouTube.Videos.List.class);

		youTubeClientStatic = mockStatic(YouTubeClient.class);
		youTubeClientStatic.when(YouTubeClient::getClient).thenReturn(youtubeMock);

		// searchVideoIds 체인
		when(youtubeMock.search()).thenReturn(searchMock);
		when(searchMock.list(anyList())).thenReturn(listMock);
		when(listMock.setQ(anyString())).thenReturn(listMock);
		when(listMock.setType(anyList())).thenReturn(listMock);
		when(listMock.setVideoLicense(anyString())).thenReturn(listMock);
		when(listMock.setOrder(anyString())).thenReturn(listMock);
		when(listMock.setRelevanceLanguage(anyString())).thenReturn(listMock);
		when(listMock.setRegionCode(anyString())).thenReturn(listMock);
		when(listMock.setMaxResults(anyLong())).thenReturn(listMock);
		when(listMock.setVideoDuration(anyString())).thenReturn(listMock);
		when(listMock.setKey(anyString())).thenReturn(listMock);
		when(listMock.execute())
			.thenThrow(new IOException("fail1"))
			.thenThrow(new IOException("fail2"))
			.thenReturn(new SearchListResponse().setItems(emptyList()));

		// fetchVideosByIdsAsync 체인
		when(youtubeMock.videos()).thenReturn(videosMock);
		when(videosMock.list(anyList())).thenReturn(videoListMock);
		when(videoListMock.setId(anyList())).thenReturn(videoListMock);
		when(videoListMock.setKey(anyString())).thenReturn(videoListMock);
	}

	@AfterEach
	void tearDown() {
		youTubeClientStatic.close();
	}

	@Test
	@DisplayName("retry: IOException 시 3회 재시도 후 정상 리턴")
	void retryUntilSuccess() throws IOException {
		List<String> ids = youtubeService.searchVideoIds("foo", "KR", "ko", null, 5);
		assertTrue(ids.isEmpty());
		verify(listMock, times(3)).execute();
	}

	@Test
	@DisplayName("timelimiter: 200ms 지연 시 100ms 타임아웃 발생")
	void timelimiterTriggersTimeout() throws IOException {
		// videoListMock.execute 에 200ms 지연
		doAnswer(inv -> { Thread.sleep(200); return new VideoListResponse().setItems(emptyList()); })
			.when(videoListMock).execute();

		// 비동기 메서드
		CompletableFuture<?> future = youtubeService.fetchVideosByIdsAsync(List.of("id1"));

		// get(timeout) 으로 직접 TimeoutException 확인
		assertThrows(TimeoutException.class, () -> future.get(150, java.util.concurrent.TimeUnit.MILLISECONDS));
	}
}
