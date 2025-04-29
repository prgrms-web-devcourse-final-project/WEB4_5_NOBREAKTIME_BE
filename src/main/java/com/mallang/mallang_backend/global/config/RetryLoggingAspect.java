package com.mallang.mallang_backend.global.config;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Retryable로 주석된 메서드에서 예외 발생할 때 마다 로그 남기기 위한 클래스
 */
@Aspect
@Component
public class RetryLoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(RetryLoggingAspect.class);

	// Retryable이 붙은 메서드가 IOException을 던질 때마다 로그를 찍음
	@AfterThrowing(pointcut = "@annotation(org.springframework.retry.annotation.Retryable)", throwing = "ex")
	public void logRetryAttempt(Exception ex) {
		log.warn("[Retry] exception occurred: {}", ex.getMessage());
	}
}