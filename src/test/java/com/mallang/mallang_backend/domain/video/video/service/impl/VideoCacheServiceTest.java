package com.mallang.mallang_backend.domain.video.video.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.mallang.mallang_backend.domain.video.video.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VideoCacheService 테스트")
class VideoCacheServiceTest {

	@Mock
	private YoutubeService youtubeService;

	@Mock
	private VideoSearchProperties youtubeSearchProperties;

	@Mock
	private CacheManager cacheManager;

	@Mock
	private RedisDistributedLock redisDistributedLock;

	private VideoCacheService service;

	@BeforeEach
	@DisplayName("테스트 설정 및 Spy 주입")
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = spy(new VideoCacheService(
			youtubeService,
			youtubeSearchProperties,
			cacheManager,
			redisDistributedLock
		));
		ReflectionTestUtils.setField(service, "self", service);

		// 기본 검색 설정 맵 생성
		VideoSearchProperties.SearchDefault sd = new VideoSearchProperties.SearchDefault();
		sd.setQuery("defaultQuery");
		sd.setRegion("US");
		var defaults = Collections.singletonMap("en", sd);
		when(youtubeSearchProperties.getDefaults()).thenReturn(defaults);
	}

	@Test
	@DisplayName("캐시 히트 후 저장된 크기가 요청 크기 이상이면 SubList 반환")
	void getFullVideoList_whenCacheHitAndStoredSizeEnough_shouldReturnSubList() {
		// given
		String q = "foo", category = "bar", language = "en";
		long fetchSize = 3;
		List<VideoResponse> fiveVideos = Collections.nCopies(5, mock(VideoResponse.class));
		CachedVideos cached = new CachedVideos(5, fiveVideos);

		doReturn(cached)
			.when(service).loadCached(q, category, language, fetchSize);

		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(true);

		// when
		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		// then
		assertThat(result).hasSize((int)fetchSize);
		verify(service).loadCached(q, category, language, fetchSize);
		verify(service, never()).fetchAndCache(anyString(), anyString(), anyString(), anyLong());
	}

	@Test
	@DisplayName("캐시 히트하지만 저장된 크기가 요청 크기보다 작으면 fetchAndCache 호출")
	void getFullVideoList_whenCacheHitButStoredSizeTooSmall_shouldCallFetchAndCache() {
		// given
		String q = "", category = "", language = "";
		long fetchSize = 7;
		CachedVideos smallCache = new CachedVideos(3, Collections.nCopies(3, mock(VideoResponse.class)));
		CachedVideos freshCache = new CachedVideos(7, Collections.nCopies(7, mock(VideoResponse.class)));

		doReturn(smallCache)
			.when(service).loadCached(q, category, language, fetchSize);
		doReturn(freshCache)
			.when(service).fetchAndCache(q, category, language, fetchSize);

		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(true);

		// when
		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		// then
		assertThat(result).hasSize((int)fetchSize);
		verify(service).loadCached(q, category, language, fetchSize);
		verify(service).fetchAndCache(q, category, language, fetchSize);
	}

	@Test
	@DisplayName("락 획득 실패 시 대기 후 진행")
	void getFullVideoList_whenLockNotAcquired_shouldWaitThenProceed() {
		// given
		String q = "x", category = "y", language = "z";
		long fetchSize = 2;
		CachedVideos cached = new CachedVideos(2, Collections.nCopies(2, mock(VideoResponse.class)));

		doReturn(cached)
			.when(service).loadCached(q, category, language, fetchSize);

		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(false);
		when(redisDistributedLock.waitForUnlockThenFetch(anyString(), anyLong(), eq(500L)))
			.thenReturn(true);

		// when
		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		// then
		assertThat(result).hasSize((int)fetchSize);
		verify(redisDistributedLock).waitForUnlockThenFetch(anyString(), anyLong(), eq(500L));
	}

	@Test
	@DisplayName("락 대기 타임아웃 시 fetchAndCache 없이 진행")
	void getFullVideoList_whenWaitTimeout_shouldProceedWithoutFetch() {
		// given
		String q = "timeout", category = "", language = "en";
		long fetchSize = 4;
		CachedVideos cached = new CachedVideos(4, Collections.nCopies(4, mock(VideoResponse.class)));

		doReturn(cached)
			.when(service).loadCached(q, category, language, fetchSize);

		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(false);
		when(redisDistributedLock.waitForUnlockThenFetch(anyString(), anyLong(), eq(500L)))
			.thenReturn(false);

		// when
		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		// then
		assertThat(result).hasSize((int)fetchSize);
		verify(service, never()).fetchAndCache(anyString(), anyString(), anyString(), anyLong());
	}

	@Test
	@DisplayName("loadCached 예외 발생 시 락 해제 및 예외 전파")
	void getFullVideoList_whenLoadCachedThrows_shouldUnlockAndRethrow() {
		// given
		String q = "error", category = "", language = "en";
		long fetchSize = 1;

		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		doThrow(new RuntimeException("boom"))
			.when(service).loadCached(q, category, language, fetchSize);
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(true);

		// when / then
		assertThatThrownBy(() -> service.getFullVideoList(q, category, language, fetchSize))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("boom");
		verify(redisDistributedLock).unlock(anyString(), anyString());
	}
	@Test
	@DisplayName("락 해제 실패 시에도 예외 없이 로직 정상 종료 및 unlock 호출 확인")
	void getFullVideoList_whenUnlockFails_shouldStillProceed() {
		// given
		String q = "unlockFail", category = "", language = "en";
		long fetchSize = 2;
		List<VideoResponse> videos = Collections.nCopies(2, mock(VideoResponse.class));
		CachedVideos cached = new CachedVideos(2, videos);

		doReturn(cached)
			.when(service).loadCached(q, category, language, fetchSize);
		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		// unlock 실패 시나리오
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(false);

		// when
		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		// then
		// 결과는 정상이며, unlock 호출이 이루어졌는지 검증
		assertThat(result).hasSize((int)fetchSize);
		verify(redisDistributedLock).unlock(anyString(), anyString());
	}
}
