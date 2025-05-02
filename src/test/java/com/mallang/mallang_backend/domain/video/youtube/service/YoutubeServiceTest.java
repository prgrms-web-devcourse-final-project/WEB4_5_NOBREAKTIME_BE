package com.mallang.mallang_backend.domain.video.youtube.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
import com.mallang.mallang_backend.domain.video.youtube.client.YouTubeClient;

@SpringBootTest
@TestPropertySource(properties = {
	"youtube.api.key=test",
	"resilience4j.retry.instances.youtubeService.max-attempts=3",
	"resilience4j.retry.instances.youtubeService.wait-duration=0ms"
})
class YoutubeServiceResilience4jTest {

	@Autowired
	private YoutubeService youtubeService;

	private MockedStatic<YouTubeClient> youtubeClientStatic;
	private YouTube.Search.List listMock;

	@BeforeEach
	void setup() throws IOException {
		YouTube youtubeMock = mock(YouTube.class);
		YouTube.Search searchMock = mock(YouTube.Search.class);
		listMock = mock(YouTube.Search.List.class);

		youtubeClientStatic = mockStatic(YouTubeClient.class);
		youtubeClientStatic.when(YouTubeClient::getClient).thenReturn(youtubeMock);

		when(youtubeMock.search()).thenReturn(searchMock);
		when(searchMock.list(anyList())).thenReturn(listMock);
		// fluent API stubbing
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
			.thenReturn(new SearchListResponse().setItems(Collections.emptyList()));
	}

	@AfterEach
	void tearDown() {
		youtubeClientStatic.close();
	}

	@Test
	@DisplayName("searchVideoIds(): IOException 발생 시 3회 재시도 후 성공해야 한다")
	void retryUntilSuccess() throws IOException {
		// 원래 시그니처로 호출
		List<String> ids = youtubeService.searchVideoIds("foo", "KR", "ko", null, 5);
		assertTrue(ids.isEmpty(), "세 번 시도 후 빈 리스트를 반환해야 합니다");
		verify(listMock, times(3)).execute();
	}
}
