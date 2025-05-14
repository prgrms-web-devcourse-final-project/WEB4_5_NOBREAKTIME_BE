package com.mallang.mallang_backend.domain.video.video.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;

@ExtendWith(MockitoExtension.class)
class VideoCacheServiceTest {

	@Mock
	private YoutubeService youtubeService;

	@Mock
	private VideoSearchProperties youtubeSearchProperties;

	@InjectMocks
	private VideoCacheService cacheService;

	@BeforeEach
	void setupDefaults() {
		// 기본 defaults 세팅
		VideoSearchProperties.SearchDefault enDefault = new VideoSearchProperties.SearchDefault();
		enDefault.setRegion("US");
		enDefault.setQuery("defaultQuery");
		Map<String, VideoSearchProperties.SearchDefault> defaults = new HashMap<>();
		defaults.put("en", enDefault);
		when(youtubeSearchProperties.getDefaults()).thenReturn(defaults);
	}

	@Test
	@DisplayName("getFullVideoList: IDs 없을 때 빈 리스트 반환")
	void getFullVideoList_whenNoIds_returnsEmpty() throws IOException {
		when(youtubeService.searchVideoIds(anyString(), anyString(), anyString(), anyString(), anyLong()))
			.thenReturn(List.of());

		List<VideoResponse> result = cacheService.getFullVideoList("q", "cat", "en", 5L);

		assertTrue(result.isEmpty());
		verify(youtubeService).searchVideoIds("q", "US", "en", "cat", 5L);
	}

	@Test
	@DisplayName("getFullVideoList: IDs 있을 때 매핑된 응답 반환")
	void getFullVideoList_whenIds_returnMappedResponses() throws IOException {
		// given
		when(youtubeService.searchVideoIds("q", "US", "en", "", 2L))
			.thenReturn(List.of("id1", "id2"));
		Video v1 = new Video(); v1.setId("id1");
		Video v2 = new Video(); v2.setId("id2");
		when(youtubeService.fetchVideosByIds(List.of("id1", "id2")))
			.thenReturn(List.of(v1, v2));

		// static mocking of VideoUtils
		try (MockedStatic<VideoUtils> utils = Mockito.mockStatic(VideoUtils.class)) {
			utils.when(() -> VideoUtils.isCreativeCommons(any())).thenReturn(true);
			utils.when(() -> VideoUtils.matchesLanguage(any(), anyString())).thenReturn(true);
			utils.when(() -> VideoUtils.isDurationLessThanOrEqualTo20Minutes(any())).thenReturn(true);
			utils.when(() -> VideoUtils.toVideoResponse(v1))
				.thenReturn(new VideoResponse("id1", "t1", "d1", "u1", false));
			utils.when(() -> VideoUtils.toVideoResponse(v2))
				.thenReturn(new VideoResponse("id2", "t2", "d2", "u2", false));

			// when
			List<VideoResponse> result = cacheService.getFullVideoList("q", "", "en", 2L);

			// then
			assertEquals(2, result.size());
			assertEquals("id1", result.get(0).getVideoId());
			assertEquals("id2", result.get(1).getVideoId());
		}
	}
}