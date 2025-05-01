package com.mallang.mallang_backend.global.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;

@Configuration
public class Resilience4jRetryLoggingConfig {

	private static final Logger log = LoggerFactory.getLogger(Resilience4jRetryLoggingConfig.class);

	private final RetryRegistry retryRegistry;

	public Resilience4jRetryLoggingConfig(RetryRegistry retryRegistry) {
		this.retryRegistry = retryRegistry;
	}

	@PostConstruct
	public void registerRetryEventListeners() {
		// application.yml에 정의한 'api Retry' 인스턴스 가져오기
		Retry retry = retryRegistry.retry("apiRetry");

		retry.getEventPublisher()
			.onRetry(event ->
				log.warn("[Retry][{}] attempt #{}, exception: {}",
					event.getName(),
					event.getNumberOfRetryAttempts(),
					event.getLastThrowable().getMessage()
				)
			)
			.onError(event -> {
				log.error("[Retry][{}] exhausted after {} attempt(s), last exception: {}",
					event.getName(),
					event.getNumberOfRetryAttempts(),
					event.getLastThrowable().getMessage()
				);
				//fallback 진입 로그
				log.error("[Fallback][{}] fallback method invoked due to retry exhaustion", event.getName());
			})
			.onSuccess(event ->
				log.info("[Retry][{}] succeeded after {} attempt(s)",
					event.getName(),
					event.getNumberOfRetryAttempts()
				)
			);
	}
}
