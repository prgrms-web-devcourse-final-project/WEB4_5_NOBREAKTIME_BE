package com.mallang.mallang_backend.global.resilience4j;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mallang.mallang_backend.global.resilience4j.code.TestService;

@SpringBootTest
@ActiveProfiles("local")
class CustomBulkheadConfigQueueingTest {

	@Autowired
	private TestService testService;

	@BeforeEach
	void setUp() {
		testService.resetBulkheadCounters();
	}

	@Test
	@DisplayName("youtubeService 벌크헤드: 11번째 호출은 앞의 10개 해제 후 진입 (대기 ≳100ms)")
	void youtubeService_bulkhead_queueing_behavior() throws InterruptedException, ExecutionException, TimeoutException {
		int concurrent = 10;
		ExecutorService exec = Executors.newFixedThreadPool(concurrent + 1);
		CountDownLatch readyLatch = new CountDownLatch(concurrent);
		CountDownLatch startLatch = new CountDownLatch(1);

		// 10개의 스레드를 띄워서 bulkheadTestMethod()에 진입시키고 100ms 동안 점유
		for (int i = 0; i < concurrent; i++) {
			exec.submit(() -> {
				try {
					readyLatch.countDown();
					startLatch.await();
					testService.bulkheadTestMethod();
				} catch (Exception ignore) {
				}
			});
		}
		// 10개 모두 준비되길 대기
		readyLatch.await();
		// 동시에 출발
		startLatch.countDown();

		// 2) 11번째 호출을 Future 로 실행, 대기 시간을 측정
		Future<Long> waiter = exec.submit(() -> {
			long t0 = System.nanoTime();
			try {
				testService.bulkheadTestMethod();
			} catch (Exception ignore) {
			}
			long t1 = System.nanoTime();
			return TimeUnit.NANOSECONDS.toMillis(t1 - t0);
		});

		// 최대 500ms 안에 완료될 거라고 보고 기다림
		long elapsedMs = waiter.get(500, TimeUnit.MILLISECONDS);

		exec.shutdownNow();

		// 11번째 호출이 최소 100ms 이상 기다렸다가 실행됐음을 검증
		assertThat(elapsedMs)
			.as("11번째 호출 대기 시간")
			.isGreaterThanOrEqualTo(100);

		assertThat(testService.getPermittedCount())
			.as("허용된 전체 호출 수")
			.isGreaterThanOrEqualTo(11);
	}
}
