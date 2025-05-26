package com.mallang.mallang_backend.domain.video.video.cache.quartz.job;

import com.mallang.mallang_backend.domain.video.video.cache.quartz.service.CacheSchedulerService;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import com.mallang.mallang_backend.global.slack.SlackNotification;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution // 중복 실행 방지
@PersistJobDataAfterExecution // 영속성
public class CacheSchedulerJob implements Job {

	private final CacheSchedulerService cacheSchedulerService;

    public CacheSchedulerJob(CacheSchedulerService cacheSchedulerService) {
        this.cacheSchedulerService = cacheSchedulerService;
    }

    @Override
	@TimeTrace
	@SlackNotification(title = "검색 캐싱", message = "현재 캐시 스케줄링이 실행 준비 중입니다.")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		var dataMap = context.getMergedJobDataMap();
		String q         = dataMap.getString("q");
		String category  = dataMap.getString("category");
		String language  = dataMap.getString("language");
		String region = dataMap.getString("region");
		long fetchSize   = dataMap.getLong("fetchSize");

		try {
			int refreshed = cacheSchedulerService.refreshCache(q, category, language, region, fetchSize);
			log.info("CacheSchedulerJob 실행 시작 검색어 {}, 카테고리 {}, 언어 {}, 요청 갯수 {}, 가져온 갯수 {}",
				q, category, language, fetchSize, refreshed);
		} catch (Exception e) {
			log.error("CacheSchedulerJob 실행에 실패했습니다.", e);
		}
	}
}
