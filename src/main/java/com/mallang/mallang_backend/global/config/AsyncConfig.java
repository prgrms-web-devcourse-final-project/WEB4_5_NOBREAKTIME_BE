package com.mallang.mallang_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

	/**
	 * 비디오 히스토리 저장용 비동기 스레드풀 설정
	 * aws t3.micro 기준
	 * corePoolSize   : 1 (기본 스레드 수)
	 * maxPoolSize    : 2 (최대 스레드 수)
	 * queueCapacity  : 20 (대기 큐 크기)
	 */
	@Bean(name = "videoHistoryExecutor")
	public Executor videoHistoryExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("videoHistory-");
		executor.initialize();
		return executor;
	}

	/**
	 * 비디오 히스토리 저장용 비동기 스레드풀 설정
	 * aws t3.micro 기준
	 * corePoolSize   : 1 (기본 스레드 수)
	 * maxPoolSize    : 2 (최대 스레드 수)
	 * queueCapacity  : 20 (대기 큐 크기)
	 */
	@Bean(name = "videoExecutor")
	public Executor videoExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("video-");
		executor.initialize();
		return executor;
	}
}
