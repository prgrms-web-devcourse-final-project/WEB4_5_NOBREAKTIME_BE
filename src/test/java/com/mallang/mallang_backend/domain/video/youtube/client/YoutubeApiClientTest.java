package com.mallang.mallang_backend.domain.video.youtube.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YoutubeApiClientTest {

	@Mock private YouTube youTube;
	@Mock private YouTube.Search youTubeSearch;
	@Mock private YouTube.Search.List searchListRequest;
	@Mock private YouTube.Videos youTubeVideos;
	@Mock private YouTube.Videos.List videoListRequest;

	private YoutubeApiClient apiClient;
	private MockedStatic<YouTubeClient> youTubeClientStatic;

	@BeforeEach
	void setUp() throws IOException {
		// static mock for YouTubeClient.getClient()
		youTubeClientStatic = mockStatic(YouTubeClient.class);
		youTubeClientStatic.when(YouTubeClient::getClient).thenReturn(youTube);

		apiClient = new YoutubeApiClient(new YouTubeClient());

		// --- searchOnce() stubbing ---
		when(youTube.search()).thenReturn(youTubeSearch);
		when(youTubeSearch.list(List.of("id"))).thenReturn(searchListRequest);

		when(searchListRequest.setQ(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setType(List.of("video"))).thenReturn(searchListRequest);
		when(searchListRequest.setVideoLicense("creativeCommon")).thenReturn(searchListRequest);
		when(searchListRequest.setOrder("relevance")).thenReturn(searchListRequest);
		when(searchListRequest.setRelevanceLanguage(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setRegionCode(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setMaxResults(anyLong())).thenReturn(searchListRequest);
		when(searchListRequest.setKey(any())).thenReturn(searchListRequest);            // <- 여기를 any() 로
		when(searchListRequest.setVideoCategoryId(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setPageToken(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setVideoDuration(anyString())).thenReturn(searchListRequest);

		// --- fetchOnce() stubbing ---
		when(youTube.videos()).thenReturn(youTubeVideos);
		when(youTubeVideos
			.list(List.of("id", "snippet", "contentDetails", "status")))
			.thenReturn(videoListRequest);
		when(videoListRequest.setId(anyList())).thenReturn(videoListRequest);
		when(videoListRequest.setKey(any())).thenReturn(videoListRequest);               // <- 여기도 any() 로
	}

	@AfterEach
	void tearDown() {
		youTubeClientStatic.close();
	}

	@Test
	@DisplayName("searchOnce(): 기본 파라미터로 요청 후 결과 리턴")
	void searchOnce_withoutOptionalParams() throws IOException {
		SearchListResponse dummy = new SearchListResponse();
		when(searchListRequest.execute()).thenReturn(dummy);

		var result = apiClient.searchOnce(
			"query", "KR", "ko", null, null, 5L, "medium"
		);

		assertSame(dummy, result);
		verify(searchListRequest, never()).setVideoCategoryId(anyString());
		verify(searchListRequest, never()).setPageToken(anyString());
	}

	@Test
	@DisplayName("searchOnce(): optional 파라미터 포함 시 setter 호출")
	void searchOnce_withOptionalParams() throws IOException {
		SearchListResponse dummy = new SearchListResponse();
		when(searchListRequest.execute()).thenReturn(dummy);

		var result = apiClient.searchOnce(
			"query", "US", "en", "15", "tok123", 10L, "medium"
		);

		assertSame(dummy, result);
		verify(searchListRequest).setVideoCategoryId("15");
		verify(searchListRequest).setPageToken("tok123");
	}

	@Test
	@DisplayName("fetchOnce(): ID 리스트로 상세조회 결과 리턴")
	void fetchOnce_success() throws IOException {
		VideoListResponse dummy = new VideoListResponse();
		when(videoListRequest.execute()).thenReturn(dummy);

		var result = apiClient.fetchOnce(List.of("id1","id2"));

		assertSame(dummy, result);
	}

	@Test
	@DisplayName("fetchOnce(): IOException 발생 시 그대로 예외 전달")
	void fetchOnce_ioException() throws IOException {
		when(videoListRequest.execute())
			.thenThrow(new IOException("ytdl fail"));

		assertThrows(IOException.class, () ->
			apiClient.fetchOnce(List.of("any"))
		);
	}
}
