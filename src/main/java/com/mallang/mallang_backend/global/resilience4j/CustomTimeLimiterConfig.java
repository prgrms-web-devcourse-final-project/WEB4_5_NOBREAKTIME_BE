package com.mallang.mallang_backend.global.resilience4j;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.exception.message.MessageService;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnErrorEvent;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnSuccessEvent;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnTimeoutEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CustomTimeLimiterConfig {

	private static final Logger log = LoggerFactory.getLogger(CustomTimeLimiterConfig.class);

	// TimeLimiterRegistry에서 모든 인스턴스 조회
	private final TimeLimiterRegistry timeLimiterRegistry;
	private final MessageService messageService;

	/**
	 * 모든 타임리미터에 이벤트 리스너 등록
	 */
	@PostConstruct
	public void init() {
		timeLimiterRegistry.getAllTimeLimiters()
			.forEach(this::attachListeners);
	}

	/**
	 * 개별 타임리미터에 시간 초과/정상 완료/오류 이벤트 리스너 등록
	 */
	private void attachListeners(TimeLimiter tl) {
		String name = tl.getName();

		tl.getEventPublisher()
			.onTimeout(this::handleTimeout)   // 시간 초과 이벤트
			.onSuccess(this::handleSuccess)   // 정상 완료 이벤트
			.onError(this::handleError);      // 기타 오류 이벤트

		log.info("타임리미터 이벤트 리스너 등록: {}", name);
	}

	/**
	 * 시간 초과
	 */
	private void handleTimeout(TimeLimiterOnTimeoutEvent e) {
		String txId = MDC.get("transactionId");
		log.error("[TimeLimiter][txId={}] '{}' 시간 초과 발생",
			txId != null ? txId : "N/A", e.getTimeLimiterName());
	}

	/**
	 * 정상 완료
	 */
	private void handleSuccess(TimeLimiterOnSuccessEvent e) {
		String txId = MDC.get("transactionId");
		log.info("[TimeLimiter][txId={}] '{}' 정상 완료",
			txId != null ? txId : "N/A", e.getTimeLimiterName());
	}

	/**
	 * 오류 발생
	 */
	private void handleError(TimeLimiterOnErrorEvent e) {
		String txId = MDC.get("transactionId");
		Throwable t = e.getThrowable();
		String msg = (t instanceof ServiceException)
			? messageService.getMessage(((ServiceException) t).getMessageCode())
			: t.getMessage();

		log.error("[TimeLimiter][txId={}] '{}' 처리 오류 - {}: {}",
			txId != null ? txId : "N/A",
			e.getTimeLimiterName(),
			t.getClass().getSimpleName(), msg);
	}
}
