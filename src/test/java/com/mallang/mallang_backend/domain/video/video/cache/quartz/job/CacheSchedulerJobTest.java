package com.mallang.mallang_backend.domain.video.video.cache.quartz.job;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mallang.mallang_backend.domain.video.video.cache.quartz.service.CacheSchedulerService;

@ExtendWith(MockitoExtension.class)
class CacheSchedulerJobTest {

	@Mock
	private CacheSchedulerService cacheSchedulerService;

	@Mock
	private JobExecutionContext context;

	@Mock
	private JobDataMap dataMap;

	@InjectMocks
	private CacheSchedulerJob job;

	@BeforeEach
	void setUp() {
		// 기본 JobDataMap 반환
		given(context.getMergedJobDataMap()).willReturn(dataMap);
		// 스케줄러 실행 시 필요한 region 값 모킹
		given(dataMap.getString("region")).willReturn("US");
	}

	@Test
	@DisplayName("execute: 정상 실행 시 refreshCache 호출")
	void execute_callsRefreshCache() throws JobExecutionException {
		// given
		given(dataMap.getString("q")).willReturn("searchTerm");
		given(dataMap.getString("category")).willReturn("cat");
		given(dataMap.getString("language")).willReturn("en");
		given(dataMap.getLong("fetchSize")).willReturn(100L);
		given(cacheSchedulerService.refreshCache("searchTerm", "cat", "en", "US", 100L)).willReturn(5);

		// when
		job.execute(context);

		// then
		then(cacheSchedulerService).should().refreshCache("searchTerm", "cat", "en", "US", 100L);
	}

	@Test
	@DisplayName("execute: refreshCache 예외 발생")
	void execute_handlesException() throws JobExecutionException {
		// given
		given(dataMap.getString("q")).willReturn("q");
		given(dataMap.getString("category")).willReturn("c");
		given(dataMap.getString("language")).willReturn("en");
		given(dataMap.getLong("fetchSize")).willReturn(50L);
		// 예외 던지도록 설정
		willThrow(new RuntimeException("fail")).given(cacheSchedulerService)
			.refreshCache(anyString(), anyString(), anyString(), anyString(), anyLong());

		// when & then
		assertDoesNotThrow(() -> job.execute(context));
		then(cacheSchedulerService).should().refreshCache("q", "c", "en", "US", 50L);
	}
}
