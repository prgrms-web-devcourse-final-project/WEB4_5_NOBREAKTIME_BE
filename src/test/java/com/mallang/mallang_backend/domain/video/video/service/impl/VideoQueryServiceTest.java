package com.mallang.mallang_backend.domain.video.video.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

@ExtendWith(MockitoExtension.class)
class VideoQueryServiceTest {

	@Mock
	private VideoCacheService cacheService;

	@InjectMocks
	private VideoQueryService queryService;

	@BeforeEach
	void setUp() {
		// no-op
	}

	@Test
	@DisplayName("queryVideos: 리스트 셔플 및 maxResults 제한 확인")
	void queryVideos_shufflesAndLimitsSize() throws IOException {
		VideoResponse a = new VideoResponse("A", "t", "d", "u", false, "P10M");
		VideoResponse b = new VideoResponse("B", "t", "d", "u", false, "P10M");
		VideoResponse c = new VideoResponse("C", "t", "d", "u", false, "P10M");
		when(cacheService.getFullVideoList("", "", "", 2L))
			.thenReturn(List.of(a, b, c));

		List<VideoResponse> out = queryService.queryVideos("", "", "", 2L);

		assertEquals(2, out.size());
		// 반환된 리스트는 원본 a,b,c 중에서 와야 함
		assertTrue(List.of(a, b, c).containsAll(out));
	}
}
