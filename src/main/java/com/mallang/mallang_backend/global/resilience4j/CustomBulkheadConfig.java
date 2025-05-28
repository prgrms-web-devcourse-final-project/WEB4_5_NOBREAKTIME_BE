package com.mallang.mallang_backend.global.resilience4j;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CustomBulkheadConfig {

	private static final Logger log = LoggerFactory.getLogger(CustomBulkheadConfig.class);

	// BulkheadRegistry에서 모든 인스턴스 조회
	private final BulkheadRegistry bulkheadRegistry;

	/**
	 * 모든 벌크헤드에 대한 이벤트 리스너 등록
	 */
	@PostConstruct
	public void init() {
		bulkheadRegistry.getAllBulkheads()
			.forEach(this::attachListeners);
	}

	/**
	 * 개별 벌크헤드에 호출 허용/거부/완료 이벤트 리스너 등록
	 */
	private void attachListeners(Bulkhead bh) {
		String name = bh.getName();

		bh.getEventPublisher()
			.onCallPermitted(this::onPermitted)   // 호출 허용
			.onCallRejected(this::onRejected)     // 호출 거부
			.onCallFinished(this::onFinished);    // 호출 완료

		log.debug("벌크헤드 이벤트 리스너 등록: {}", name);
	}

	/**
	 * 호출 허용
	 */
	private void onPermitted(BulkheadOnCallPermittedEvent e) {
		String txId = MDC.get("transactionId");
		log.info("[Bulkhead][txId={}] '{}' 호출 허용",
			txId != null ? txId : "N/A", e.getBulkheadName());
	}

	/**
	 * 호출 거부
	 */
	private void onRejected(BulkheadOnCallRejectedEvent e) {
		String txId = MDC.get("transactionId");
		log.warn("[Bulkhead][txId={}] '{}' 호출 거부",
			txId != null ? txId : "N/A", e.getBulkheadName());
	}

	/**
	 * 호출 완료
	 */
	private void onFinished(BulkheadOnCallFinishedEvent e) {
		String txId = MDC.get("transactionId");
		log.debug("[Bulkhead][txId={}] '{}' 호출 완료",
			txId != null ? txId : "N/A", e.getBulkheadName());
	}
}
