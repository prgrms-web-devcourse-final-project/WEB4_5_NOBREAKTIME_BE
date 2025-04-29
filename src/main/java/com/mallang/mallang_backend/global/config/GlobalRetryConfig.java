package com.mallang.mallang_backend.global.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * Spring Retry 활성화 및 전역 재시도 인터셉터 설정
 * 사용법 -> @Retryable(retryFor = IOException.class, interceptor = "retryOperationsInterceptor")
 * @param retryFor 재시도를 수행할 예외 클래스
 * @param interceptor 사용할 재시도 설정을 정의한 빈 이름
 */
@Configuration
@EnableRetry
public class GlobalRetryConfig {

	@Bean
	public RetryOperationsInterceptor retryOperationsInterceptor() {
		return RetryInterceptorBuilder.stateless()
			.maxAttempts(5) // 재시도 횟수
			.backOffOptions(1000, 1.0, 0) // 재시도 간격 1초, 지연 배수 1.0, 최대 지연 제한없음
			.build();
	}
}
