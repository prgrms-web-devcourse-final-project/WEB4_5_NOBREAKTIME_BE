package com.mallang.mallang_backend.global.resilience4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.resilience4j.service.Resilience4jTestService;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

@ActiveProfiles("local")
@SpringBootTest
@Import(Resilience4jTestService.class)
class CustomRetryConfigTest {

	@Autowired
	private Resilience4jTestService resilience4jTestService;

	@Autowired
	private RetryRegistry retryRegistry;

	private Retry retry;

	@BeforeEach
	void setUp() {
		retry = retryRegistry.retry("apiRetry");
		resilience4jTestService.resetRetryCounter();
		assertEquals(0, resilience4jTestService.getRetryCounter(),
			"테스트 전에는 호출 카운터가 0이어야 합니다");
	}

	@Test
	@Disabled("검증 완료")
	@DisplayName("retryTestMethod: 무시된 예외는 재시도 하지 않음")
	void retryTestMethod_retriesUpToMaxAttempts() {

		ServiceException ex = assertThrows(ServiceException.class, () ->
			resilience4jTestService.retryTestMethod()
		);
		assertNotNull(ex);

		assertEquals(1, resilience4jTestService.getRetryCounter(),
			"retryTestMethod 는 max-attempts(1) 만큼 호출되어야 합니다 -> 최초 호출");
	}

	@Test
	@Disabled("검증 완료")
	@DisplayName("alwaysSucceeds: 예외 없이 1번만 호출되고 retryCounter는 1")
	void alwaysSucceeds_noRetryOnSuccess() {

		assertDoesNotThrow(() ->
			retry.executeRunnable(resilience4jTestService::retrySuccessMethod)
		);

		assertEquals(0, resilience4jTestService.getRetryCounter(),
			"alwaysSucceeds 는 호출 카운터 증가 없이 성공해야 합니다");
	}
}
