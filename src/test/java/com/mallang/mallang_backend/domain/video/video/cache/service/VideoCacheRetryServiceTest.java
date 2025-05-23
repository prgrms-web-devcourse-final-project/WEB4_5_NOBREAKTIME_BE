package com.mallang.mallang_backend.domain.video.video.cache.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.mallang.mallang_backend.domain.video.video.cache.VideoCacheRetryService;
import com.mallang.mallang_backend.domain.video.video.cache.dto.CachedVideos;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class VideoCacheRetryServiceTest {

	@Mock
	private CacheManager cacheManager;
	@Mock
	private Cache cache;

	private com.mallang.mallang_backend.domain.video.video.cache.VideoCacheRetryService service;

	@BeforeEach
	void setUp() {
		service = new com.mallang.mallang_backend.domain.video.video.cache.VideoCacheRetryService(cacheManager);
	}

	@Test
	@DisplayName("getCachedVideos: 캐시에서 정상적으로 꺼내오기")
	void getCachedVideos_success() {
		// given
		String key = "testKey";
		CachedVideos stored = new CachedVideos(2, List.of(/* ... */));
		when(cacheManager.getCache("videoListCache")).thenReturn(cache);
		when(cache.get(key, CachedVideos.class)).thenReturn(stored);

		// when
		CachedVideos result = service.getCachedVideos(key);

		// then
		assertSame(stored, result);
	}

	@Test
	@DisplayName("getCachedVideos: 캐시 네임이 없으면 API_ERROR 예외")
	void getCachedVideos_cacheNotFound() {
		when(cacheManager.getCache("videoListCache")).thenReturn(null);

		ServiceException ex = assertThrows(ServiceException.class, () ->
			service.getCachedVideos("anyKey")
		);
		assertEquals(ErrorCode.API_ERROR, ex.getErrorCode());
	}

	@Test
	@DisplayName("getCachedVideos: 캐시에 값이 없으면 API_ERROR 예외")
	void getCachedVideos_cacheEmpty() {
		when(cacheManager.getCache("videoListCache")).thenReturn(cache);
		when(cache.get("anyKey", CachedVideos.class)).thenReturn(null);

		ServiceException ex = assertThrows(ServiceException.class, () ->
			service.getCachedVideos("anyKey")
		);
		assertEquals(ErrorCode.API_ERROR, ex.getErrorCode());
	}

	@Test
	@DisplayName("fallbackGetCachedVideos: 예외 발생 시 빈 응답 리스트 반환")
	void fallbackGetCachedVideos_returnsEmpty() throws Exception {
		// private 메서드라 reflection 으로 호출
		Method fallback = VideoCacheRetryService.class
			.getDeclaredMethod("fallbackGetCachedVideos", String.class, Throwable.class);
		fallback.setAccessible(true);

		CachedVideos empty = (CachedVideos) fallback.invoke(
			service, "someKey", new RuntimeException("oops")
		);

		assertNotNull(empty);
		// getTotalCount()가 없으면 이렇게 응답 리스트 크기로 검증
		assertTrue(empty.getResponses().isEmpty(), "fallback에서는 빈 응답 리스트를 반환해야 합니다");
	}
}
