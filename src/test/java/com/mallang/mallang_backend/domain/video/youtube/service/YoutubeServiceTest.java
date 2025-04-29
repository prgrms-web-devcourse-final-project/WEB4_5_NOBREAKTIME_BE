package com.mallang.mallang_backend.domain.video.youtube.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.mallang.mallang_backend.global.config.GlobalRetryConfig;
import com.mallang.mallang_backend.domain.video.youtube.client.YouTubeClient;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { GlobalRetryConfig.class, YoutubeService.class })
class YoutubeServiceTest {

	@Autowired
	private YoutubeService youtubeService;

	private MockedStatic<YouTubeClient> youTubeClientStatic;

	@BeforeEach
	void setup() throws IOException {
		// 1) YouTube, Search, List API mock 객체 생성
		YouTube youtubeMock = mock(YouTube.class);
		YouTube.Search searchMock = mock(YouTube.Search.class);
		YouTube.Search.List listMock = mock(YouTube.Search.List.class);

		// 2) static YouTubeClient.getClient() 호출을 가로채서 youtubeMock 반환
		youTubeClientStatic = mockStatic(YouTubeClient.class);
		youTubeClientStatic.when(YouTubeClient::getClient).thenReturn(youtubeMock);

		// 3) youtubeMock.search() → searchMock, searchMock.list(...) → listMock
		when(youtubeMock.search()).thenReturn(searchMock);
		when(searchMock.list(anyList())).thenReturn(listMock);

		// 4) listMock.execute()는 첫 두 번 IOException, 세 번째는 빈 결과
		when(listMock.execute())
			.thenThrow(new IOException("fail1"))
			.thenThrow(new IOException("fail2"))
			.thenReturn(new SearchListResponse().setItems(Collections.emptyList()));
	}

	@AfterEach
	void tearDown() {
		youTubeClientStatic.close();
	}

	@Test
	void retryUntilSuccess() throws IOException {
		// IOException → retry → retry → 성공 (빈 리스트)
		List<String> ids = youtubeService.searchVideoIds("foo", "KR", "ko", null, 5);
		assertTrue(ids.isEmpty(), "세 번 시도 후 빈 리스트를 반환해야 합니다");
	}
}
