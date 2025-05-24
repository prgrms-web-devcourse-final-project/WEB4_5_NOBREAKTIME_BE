package com.mallang.mallang_backend.domain.video.video.cache.quartz.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mallang.mallang_backend.domain.video.cache.CacheSchedulerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DisallowConcurrentExecution // 중복 실행 방지
@PersistJobDataAfterExecution // 영속성
public class CacheSchedulerJob implements Job {

	@Autowired
	private CacheSchedulerService cacheSchedulerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		var dataMap = context.getMergedJobDataMap();
		String q         = dataMap.getString("q");
		String category  = dataMap.getString("category");
		String language  = dataMap.getString("language");
		long fetchSize   = dataMap.getLong("fetchSize");

		try {
			int refreshed = cacheSchedulerService.refreshCache(q, category, language, fetchSize);
			log.info("CacheSchedulerJob 실행 시작 검색어 {}, 카테고리 {}, 언어 {}, 요청 갯수 {}, 가져온 갯수 {}",
				q, category, language, fetchSize, refreshed);
		} catch (Exception e) {
			log.error("CacheSchedulerJob 실행에 실패했습니다.", e);
		}
	}
}
