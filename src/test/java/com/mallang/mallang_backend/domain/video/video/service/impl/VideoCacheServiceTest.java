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

import com.mallang.mallang_backend.domain.video.video.cache.client.VideoCacheClient;
import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.domain.video.video.cache.service.VideoCacheService;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VideoCacheService 테스트")
class VideoCacheServiceTest {

	@Mock
	private VideoCacheClient cacheClient;

	@Mock
	private CacheManager cacheManager;

	@Mock
	private RedisDistributedLock redisDistributedLock;

	private VideoCacheService service;

	@BeforeEach
	@DisplayName("테스트 설정 및 Spy 주입")
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = new VideoCacheService(cacheClient, cacheManager, redisDistributedLock);
	}

	@Test
	@DisplayName("캐시 히트 후 저장된 크기가 요청 크기 이상이면 SubList 반환")
	void whenCacheHitAndStoredSizeEnough_thenReturnSubList() {
		String q = "foo", category = "bar", language = "en";
		long fetchSize = 3;
		List<VideoResponse> videos = Collections.nCopies(5, mock(VideoResponse.class));
		CachedVideos cached = new CachedVideos(5, videos);

		when(cacheClient.loadCached(q, category, language, fetchSize)).thenReturn(cached);
		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(true);

		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		assertThat(result).hasSize((int) fetchSize);
		verify(cacheClient).loadCached(q, category, language, fetchSize);
		verify(cacheClient, never()).fetchAndCache(anyString(), anyString(), anyString(), anyLong());
	}

	@Test
	@DisplayName("캐시 히트하지만 저장된 크기가 요청 크기보다 작으면 fetchAndCache 호출")
	void whenCacheHitButSizeTooSmall_thenCallFetchAndCache() {
		String q = "", category = "", language = "";
		long fetchSize = 7;
		CachedVideos small = new CachedVideos(3, Collections.nCopies(3, mock(VideoResponse.class)));
		CachedVideos fresh = new CachedVideos(7, Collections.nCopies(7, mock(VideoResponse.class)));

		when(cacheClient.loadCached(q, category, language, fetchSize)).thenReturn(small);
		when(cacheClient.fetchAndCache(q, category, language, fetchSize)).thenReturn(fresh);
		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(true);

		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		assertThat(result).hasSize((int) fetchSize);
		verify(cacheClient).loadCached(q, category, language, fetchSize);
		verify(cacheClient).fetchAndCache(q, category, language, fetchSize);
	}

	@Test
	@DisplayName("락 획득 실패 시 대기 후 진행")
	void whenLockNotAcquired_thenWaitAndProceed() {
		String q = "x", category = "y", language = "z";
		long fetchSize = 2;
		CachedVideos cached = new CachedVideos(2, Collections.nCopies(2, mock(VideoResponse.class)));

		when(cacheClient.loadCached(q, category, language, fetchSize)).thenReturn(cached);
		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(false);
		when(redisDistributedLock.waitForUnlockThenFetch(anyString(), anyLong(), eq(500L))).thenReturn(true);

		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		assertThat(result).hasSize((int) fetchSize);
		verify(redisDistributedLock).waitForUnlockThenFetch(anyString(), anyLong(), eq(500L));
	}

	@Test
	@DisplayName("락 대기 타임아웃 시 예외 발생")
	void whenWaitTimeout_thenThrowServiceException() {
		String q = "timeout", category = "", language = "en";
		long fetchSize = 4;
		CachedVideos cached = new CachedVideos(4, Collections.nCopies(4, mock(VideoResponse.class)));

		when(cacheClient.loadCached(q, category, language, fetchSize)).thenReturn(cached);
		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(false);
		when(redisDistributedLock.waitForUnlockThenFetch(anyString(), anyLong(), eq(500L))).thenReturn(false);

		assertThatThrownBy(() -> service.getFullVideoList(q, category, language, fetchSize))
			.isInstanceOf(ServiceException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.CACHE_LOCK_TIMEOUT);
	}

	@Test
	@DisplayName("loadCached 예외 발생 시 락 해제 및 예외 전파")
	void whenLoadCachedThrows_thenUnlockAndRethrow() {
		String q = "error", category = "", language = "en";
		long fetchSize = 1;

		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		when(cacheClient.loadCached(q, category, language, fetchSize)).thenThrow(new RuntimeException("boom"));
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(true);

		assertThatThrownBy(() -> service.getFullVideoList(q, category, language, fetchSize))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("boom");
		verify(redisDistributedLock).unlock(anyString(), anyString());
	}

	@Test
	@DisplayName("락 해제 실패 시 재시도 후 정상 종료")
	void whenUnlockFails_thenRetryAndProceed() {
		String q = "unlockFail", category = "", language = "en";
		long fetchSize = 2;
		CachedVideos cached = new CachedVideos(2, Collections.nCopies(2, mock(VideoResponse.class)));

		when(cacheClient.loadCached(q, category, language, fetchSize)).thenReturn(cached);
		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
		when(redisDistributedLock.unlock(anyString(), anyString()))
			.thenReturn(false)
			.thenReturn(true);

		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);
		assertThat(result).hasSize((int) fetchSize);
		verify(redisDistributedLock, times(2)).unlock(anyString(), anyString());
	}

	@Test
	@DisplayName("락 못잡았지만 대기 성공 후 저장된 크기 작으면 fetchAndCache 호출")
	void whenLockNotAcquiredButWaitSucceededAndSizeTooSmall_thenCallFetchAndCache() {
		// given
		String q = "q", category = "c", language = "l";
		long fetchSize = 5;
		// loadCached 반환값 rawFetchSize < fetchSize
		CachedVideos small = new CachedVideos(3, Collections.nCopies(3, mock(VideoResponse.class)));
		CachedVideos fresh = new CachedVideos(5, Collections.nCopies(5, mock(VideoResponse.class)));

		when(cacheClient.loadCached(q, category, language, fetchSize)).thenReturn(small);
		when(cacheClient.fetchAndCache(q, category, language, fetchSize)).thenReturn(fresh);

		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(false);
		when(redisDistributedLock.waitForUnlockThenFetch(anyString(), anyLong(), eq(500L))).thenReturn(true);
		when(redisDistributedLock.unlock(anyString(), anyString())).thenReturn(true);

		// when
		List<VideoResponse> result = service.getFullVideoList(q, category, language, fetchSize);

		// then
		assertThat(result).hasSize((int) fetchSize);
		verify(cacheClient).loadCached(q, category, language, fetchSize);
		verify(cacheClient).fetchAndCache(q, category, language, fetchSize);
	}
}
