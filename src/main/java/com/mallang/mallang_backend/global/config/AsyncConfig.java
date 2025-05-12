package com.mallang.mallang_backend.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

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
	 * 오디오 삭제 비동기 스레드풀 설정
	 */
	@Bean(name = "audioDeleteExecutor")
	public Executor videoExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(3);
		executor.setQueueCapacity(30);
		executor.setThreadNamePrefix("audioDelete-");
		executor.initialize();
		return executor;
	}

	/**
	 * 단어 저장 비동기 스레드풀 설정
	 */
	@Bean(name = "addWordExecutor")
	public Executor addWordExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(3);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("addWord-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "securityTaskExecutor")
	public Executor securityTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5); // 기본 스레드 풀 사이즈
		executor.setMaxPoolSize(10); // 최대 스레드 풀 사이즈
		executor.setQueueCapacity(20); // 작업 대기 큐 크기
		executor.setKeepAliveSeconds(60); // 60 초 대기 후 초과 스레드 삭제
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 거절 정책
		executor.setThreadNamePrefix("Social-Async-");
		executor.initialize();

		/**
		 * 시큐리티 -> 스레드 로컬에 사용자 인증 정보를 저장
		 * 스레드 풀 사용 시, 새 스레드는 기존 스레드의 ThreadLocal 값을 상속받지 않아 SecurityContext가 유실
		 *
		 * DelegatingSecurityContextAsyncTaskExecutor :
		 * 작업 실행 시 SecurityContext 를 새 스레드에 복사하는 래퍼를 생성
		 */
		return new DelegatingSecurityContextAsyncTaskExecutor(executor);
	}

	/**
	 * YouTube API 호출 전용 스레드풀
	 * corePoolSize : 5
	 * maxPoolSize  : 10
	 * queueCapacity: 100
	 */
	@Bean(name = "youtubeApiExecutor")
	public Executor youtubeApiExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("youtube-api-");
		executor.initialize();
		return executor;
	}
}
