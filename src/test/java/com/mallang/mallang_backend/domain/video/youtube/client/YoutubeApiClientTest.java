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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YoutubeApiClientTest {

	@Mock private YouTube youTube;                             // YouTube 클라이언트 모의 객체
	@Mock private YouTube.Search youTubeSearch;               // Search 하위 객체 모의
	@Mock private YouTube.Search.List searchListRequest;      // search.list 요청 모의
	@Mock private YouTube.Videos youTubeVideos;               // Videos 하위 객체 모의
	@Mock private YouTube.Videos.List videoListRequest;       // videos.list 요청 모의

	@Mock private HttpRequest httpRequest;                    // buildHttpRequest() 리턴값 모의

	private YoutubeApiClient apiClient;
	private MockedStatic<YoutubeClient> youTubeClientStatic;   // YoutubeClient.getClient() static 모의

	@BeforeEach
	void setUp() throws IOException {
		// YoutubeClient.getClient()를 static으로 모의 처리
		youTubeClientStatic = mockStatic(YoutubeClient.class);
		youTubeClientStatic.when(YoutubeClient::getClient).thenReturn(youTube);

		// 테스트 대상 인스턴스 생성
		apiClient = new YoutubeApiClient(new YoutubeClient());

		// --- searchOnce() 기본 스텁 설정 ---
		when(youTube.search()).thenReturn(youTubeSearch);
		when(youTubeSearch.list(List.of("id"))).thenReturn(searchListRequest);

		when(searchListRequest.setQ(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setType(List.of("video"))).thenReturn(searchListRequest);
		when(searchListRequest.setVideoLicense("creativeCommon")).thenReturn(searchListRequest);
		when(searchListRequest.setOrder("relevance")).thenReturn(searchListRequest);
		when(searchListRequest.setRelevanceLanguage(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setRegionCode(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setMaxResults(anyLong())).thenReturn(searchListRequest);
		when(searchListRequest.setVideoDuration(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setKey(any())).thenReturn(searchListRequest);
		when(searchListRequest.setVideoCategoryId(anyString())).thenReturn(searchListRequest);
		when(searchListRequest.setPageToken(anyString())).thenReturn(searchListRequest);

		// buildHttpRequest() 호출 시 모의 HttpRequest 반환하도록 설정
		when(searchListRequest.buildHttpRequest()).thenReturn(httpRequest);
		// getUrl() 호출 시 널이 아닌 GenericUrl 반환 (로그 출력을 위해)
		when(httpRequest.getUrl()).thenReturn(new GenericUrl("http://dummy.youtube.api/search"));

		// --- fetchOnce() 기본 스텁 설정 ---
		when(youTube.videos()).thenReturn(youTubeVideos);
		when(youTubeVideos.list(List.of("id", "snippet", "contentDetails", "status")))
			.thenReturn(videoListRequest);
		when(videoListRequest.setId(anyList())).thenReturn(videoListRequest);
		when(videoListRequest.setKey(any())).thenReturn(videoListRequest);
	}

	@AfterEach
	void tearDown() {
		// static 모의 객체 해제
		youTubeClientStatic.close();
	}

	@Test
	@DisplayName("searchOnce(): 기본 파라미터로 요청 후 결과 리턴")
	void searchOnce_withoutOptionalParams() throws IOException {
		// execute() 리턴값 모의
		SearchListResponse dummy = new SearchListResponse();
		when(searchListRequest.execute()).thenReturn(dummy);

		// 실제 호출
		var result = apiClient.searchOnce("query", "JP", "ja", null, null, 5L, "medium");

		// 결과 검증
		assertSame(dummy, result);
		verify(searchListRequest, never()).setVideoCategoryId(anyString());
		verify(searchListRequest, never()).setPageToken(anyString());
	}

	@Test
	@DisplayName("searchOnce(): optional 파라미터 포함 시 setter 호출")
	void searchOnce_withOptionalParams() throws IOException {
		SearchListResponse dummy = new SearchListResponse();
		when(searchListRequest.execute()).thenReturn(dummy);

		var result = apiClient.searchOnce("query", "US", "en", "15", "tok123", 10L, "medium");

		assertSame(dummy, result);
		verify(searchListRequest).setVideoCategoryId("15");
		verify(searchListRequest).setPageToken("tok123");
	}

	@Test
	@DisplayName("fetchOnce(): ID 리스트로 상세조회 결과 리턴")
	void fetchOnce_success() throws IOException {
		VideoListResponse dummy = new VideoListResponse();
		when(videoListRequest.execute()).thenReturn(dummy);

		var result = apiClient.fetchOnce(List.of("id1", "id2"));

		assertSame(dummy, result);
	}

	@Test
	@DisplayName("fetchOnce(): IOException 발생 시 그대로 예외 전달")
	void fetchOnce_ioException() throws IOException {
		when(videoListRequest.execute()).thenThrow(new IOException("ytdl fail"));

		assertThrows(IOException.class, () -> apiClient.fetchOnce(List.of("any")));
	}
}
