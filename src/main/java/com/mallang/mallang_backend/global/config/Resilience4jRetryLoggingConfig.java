package com.mallang.mallang_backend.global.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import io.github.resilience4j.retry.event.RetryOnSuccessEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class Resilience4jRetryLoggingConfig {

	// 로거 정의
	private static final Logger log = LoggerFactory.getLogger(Resilience4jRetryLoggingConfig.class);

	// Retry 인스턴스들을 관리하는 Registry 주입
	private final RetryRegistry retryRegistry;

	// ThreadLocal을 사용해 재시도 중 발생한 예외들을 저장할 리스트
	private final ThreadLocal<List<Throwable>> retryExceptions = ThreadLocal.withInitial(ArrayList::new);

	public Resilience4jRetryLoggingConfig(RetryRegistry retryRegistry) {
		this.retryRegistry = retryRegistry;
	}

	/**
	 * 빈이 초기화된 후 실행
	 * yml 등에 정의된 "apiRetry" 인스턴스를 가져와 이벤트 리스너 등록
	 */
	@PostConstruct
	public void registerRetryEventListeners() {
		Retry retry = retryRegistry.retry("apiRetry");

		retry.getEventPublisher()
			// 재시도 시마다 호출
			.onRetry(event -> onRetry(event))
			// 최대 재시도 후 실패 시 호출
			.onError(event -> onError(event))
			// 재시도 성공 시 호출
			.onSuccess(event -> onSuccess(event));
	}

	/**
	 * 재시도할 때마다 호출되는 콜백
	 * - 발생한 예외를 ThreadLocal 리스트에 추가
	 * - 재시도 횟수, 예외 타입·메시지, 다음 대기시간, 트랜잭션 ID 로그 출력
	 */
	private void onRetry(RetryOnRetryEvent event) {
		// 예외 누적
		retryExceptions.get().add(event.getLastThrowable());

		// MDC에서 트랜잭션 ID 가져오기 (없으면 N/A)
		String txId = MDC.get("transactionId");
		log.warn("[Retry][{}][txId={}] attempt #{} failed - {} ({}) - next retry in {} ms",
			event.getName(),
			txId != null ? txId : "N/A",
			event.getNumberOfRetryAttempts(),
			event.getLastThrowable().getClass().getSimpleName(),  // 예외 타입
			event.getLastThrowable().getMessage(),               // 예외 메시지
			event.getWaitInterval().toMillis()                  // 다음 재시도 대기 시간
		);
	}

	/**
	 * 모든 재시도가 실패하고 더 이상 재시도하지 않을 때 호출되는 콜백
	 * - 마지막 예외도 리스트에 추가
	 * - 누적된 모든 예외를 한 번에 로그로 출력
	 * - fallback 진입 로그 출력
	 * - ThreadLocal 정리
	 */
	private void onError(RetryOnErrorEvent event) {
		// 마지막 예외 추가
		retryExceptions.get().add(event.getLastThrowable());

		String txId = MDC.get("transactionId");
		// 리스트에 담긴 모든 예외 메시지 추출
		List<String> allErrors = retryExceptions.get().stream()
			.map(t -> t.getClass().getSimpleName() + ": " + t.getMessage())
			.collect(Collectors.toList());

		log.error("[Retry][{}][txId={}] exhausted after {} attempts, errors: {}",
			event.getName(),
			txId != null ? txId : "N/A",
			event.getNumberOfRetryAttempts(),
			allErrors
		);
		log.error("[Fallback][{}][txId={}] invoking fallback method",
			event.getName(),
			txId != null ? txId : "N/A"
		);

		// ThreadLocal 내용 비우기
		retryExceptions.remove();
	}

	/**
	 * 재시도가 성공했을 때 호출되는 콜백
	 * - 최종 성공까지 소요된 시도 횟수와 트랜잭션 ID 로그 출력
	 * - ThreadLocal 정리
	 */
	private void onSuccess(RetryOnSuccessEvent event) {
		String txId = MDC.get("transactionId");
		log.info("[Retry][{}][txId={}] succeeded after {} attempts",
			event.getName(),
			txId != null ? txId : "N/A",
			event.getNumberOfRetryAttempts()
		);

		// 성공 시에도 ThreadLocal 초기화
		retryExceptions.remove();
	}
}
