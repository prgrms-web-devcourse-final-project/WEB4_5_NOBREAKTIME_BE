package com.mallang.mallang_backend.domain.video.video.cache.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.video.video.cache.client.VideoCacheClient;
import com.mallang.mallang_backend.domain.video.video.cache.quartz.service.CacheSchedulerService;
import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

@ExtendWith(MockitoExtension.class)
class CacheSchedulerServiceTest {

	@Mock
	private VideoCacheClient cacheClient;

	@InjectMocks
	private CacheSchedulerService service;

	@Test
	@DisplayName("refreshCache: fetchAndCacheWithRegion 호출 후 응답 크기 반환")
	void refreshCache_returnsResponseSize() {
		// given
		String q = "query";
		String category = "cat";
		String language = "en";
		String region = "US";
		long fetchSize = 5L;
		CachedVideos stub = new CachedVideos(fetchSize, List.of(
			new VideoResponse("id1", "title1", "desc1", "thumb1", false, "PT1M"),
			new VideoResponse("id2", "title2", "desc2", "thumb2", true, "PT2M"),
			new VideoResponse("id3", "title3", "desc3", "thumb3", false, "PT3M")
		));
		given(cacheClient.fetchAndCacheWithRegion(q, category, language, region, fetchSize)).willReturn(stub);

		// when
		int result = service.refreshCache(q, category, language, region, fetchSize);

		// then
		then(cacheClient).should().fetchAndCacheWithRegion(q, category, language, region, fetchSize);
		assertEquals(3, result);
	}

	@Test
	@DisplayName("refreshCache: 빈 리스트인 경우 0 반환")
	void refreshCache_emptyResponses_returnsZero() {
		// given
		String region = "KR";
		long fetchSize = 10L;
		CachedVideos stub = new CachedVideos(fetchSize, List.of());
		given(cacheClient.fetchAndCacheWithRegion(null, null, null, region, fetchSize)).willReturn(stub);

		// when
		int result = service.refreshCache(null, null, null, region, fetchSize);

		// then
		then(cacheClient).should().fetchAndCacheWithRegion(null, null, null, region, fetchSize);
		assertEquals(0, result);
	}
}
