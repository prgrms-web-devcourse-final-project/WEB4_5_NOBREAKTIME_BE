package com.mallang.mallang_backend.global.resilience4j;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mallang.mallang_backend.global.exception.message.MessageService;
import com.mallang.mallang_backend.global.resilience4j.code.TestService;

import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("local")  //
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestService.class})
class CustomTimeLimiterConfigTest {

	@Autowired
	private TestService testService;

	@Autowired
	private TimeLimiterRegistry timeLimiterRegistry;

	@MockBean
	private MessageService messageService;

	@Test
	@DisplayName("TimeLimiter: 정상 완료 시 즉시 반환, MessageService 미호출")
	void timeoutSuccessMethod_invokesSuccessEvent() throws Exception {
		// given: TimeLimiter 인스턴스가 registry에 등록되어 있어야 함
		assertThat(timeLimiterRegistry.getAllTimeLimiters())
			.anyMatch(tl -> tl.getName().equals("youtubeService"));

		// when
		String result = testService.timeoutSuccessMethod()
			.toCompletableFuture()
			.get(100, TimeUnit.MILLISECONDS);

		// then
		assertEquals("OK", result);

		verify(messageService, never()).getMessage(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("TimeLimiter: 지연 6초 → 타임아웃 발생 (TimeoutException)")
	void timeoutTestMethod_throwsTimeout() {
		// when & then
		ExecutionException ex = assertThrows(ExecutionException.class, () ->
			testService.timeoutTestMethod()
				.toCompletableFuture()
				.get(1, TimeUnit.MINUTES)
		);
		// 원인이 TimeoutException 이어야 함
		assertThat(ex.getCause()).isInstanceOf(TimeoutException.class);

		// TimeoutException 은 ServiceException 이 아니므로 messageService 역시 호출되지 않음
		verify(messageService, never()).getMessage(org.mockito.ArgumentMatchers.any());
	}
}
