package com.mallang.mallang_backend.global.resilience4j;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mallang.mallang_backend.global.exception.message.MessageService;
import com.mallang.mallang_backend.global.resilience4j.code.TestService;

import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnErrorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestService.class})
class CustomTimeLimiterConfigTest {

	@Autowired
	private TestService testService;

	@Autowired
	private TimeLimiterRegistry timeLimiterRegistry;

	@MockitoBean
	private MessageService messageService;

	@Test
	@DisplayName("TimeLimiter: 정상 완료 시 즉시 반환, MessageService 미호출")
	void timeoutSuccessMethod_invokesSuccessEvent() throws Exception {
		// given: registry에 'youtubeService' 타임리미터가 등록되어 있어야 함
		assertThat(timeLimiterRegistry.getAllTimeLimiters())
			.anyMatch(tl -> tl.getName().equals("youtubeService"));

		// when
		String result = testService.timeoutSuccessMethod()
			.toCompletableFuture()
			.get(100, TimeUnit.MILLISECONDS);

		// then
		assertEquals("OK", result);
		verify(messageService, never()).getMessage(anyString());
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
		// cause 가 TimeoutException 이어야 함
		assertThat(ex.getCause()).isInstanceOf(TimeoutException.class);

		// 타임아웃 시에도 MessageService 호출이 없어야 한다
		verify(messageService, never()).getMessage(anyString());
	}

	@Test
	@DisplayName("handleError: 일반 예외 발생 시 messageService 미호출 (DI 없이)")
	void handleError_directInstance_withOtherException() throws Exception {
		MessageService msgMock = mock(MessageService.class);
		CustomTimeLimiterConfig config = new CustomTimeLimiterConfig(null, msgMock);

		RuntimeException re = new RuntimeException("oops");
		TimeLimiterOnErrorEvent evt = mock(TimeLimiterOnErrorEvent.class);
		when(evt.getThrowable()).thenReturn(re);
		when(evt.getTimeLimiterName()).thenReturn("someLimiter");

		Method m = CustomTimeLimiterConfig.class
			.getDeclaredMethod("handleError", TimeLimiterOnErrorEvent.class);
		m.setAccessible(true);
		m.invoke(config, evt);

		// 일반 예외인 경우 호출이 없어야 함
		verifyNoInteractions(msgMock);
	}
}
