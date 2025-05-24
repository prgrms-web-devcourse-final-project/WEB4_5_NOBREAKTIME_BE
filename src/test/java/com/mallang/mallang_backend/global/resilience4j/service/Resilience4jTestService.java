package com.mallang.mallang_backend.global.resilience4j.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class Resilience4jTestService {

    // ── 서킷 브레이커 테스트용 카운터 ────────────────────────────────────
    private int cbCounter = 1;

    @CircuitBreaker(name = "oauthUserLoginService", fallbackMethod = "cbFallback")
    public void testMethod() {
        log.info("CircuitBreaker 테스트 호출 – 시도 #{}", cbCounter++);
        throw new ServiceException(ErrorCode.API_ERROR);
    }

    @CircuitBreaker(name = "oauthUserLoginService", fallbackMethod = "cbFallback")
    public void successMethod() {
        log.info("CircuitBreaker 성공 메서드 호출");
    }

    private void cbFallback(Exception e) {
        if (e instanceof CallNotPermittedException) {
            log.warn("CircuitBreaker 호출 중지 – {}", e.getClass().getSimpleName());
        }
        throw new ServiceException(ErrorCode.API_ERROR, e);
    }

    // ── 리트라이 테스트용 카운터 ───────────────────────────────────────────
    @Getter
    private int retryCounter = 0;

    public void resetRetryCounter() {
        this.retryCounter = 0;
    }

    @Retry(name = "apiRetry", fallbackMethod = "retryFallback")
    public void retryTestMethod() {
        retryCounter++;
        log.info("Retry 테스트 호출 – 시도 #{}", retryCounter);
        throw new ServiceException(ErrorCode.API_ERROR);
    }

    @Retry(name = "apiRetry", fallbackMethod = "retryFallback")
    public void retrySuccessMethod() {
        log.info("Retry 성공 메서드 호출");
    }

    private void retryFallback(Throwable e) {
        log.info("Retry fallback 호출 – {}", e.getClass().getSimpleName());
        if (e instanceof ServiceException) {
            throw (ServiceException) e;
        }
        throw new ServiceException(ErrorCode.API_ERROR, e);
    }

    // ─── 벌크헤드 테스트용 ───────────────────────────────
    private final AtomicInteger permittedCount = new AtomicInteger();
    private final AtomicInteger rejectedCount  = new AtomicInteger();

    @Bulkhead(name = "youtubeService", fallbackMethod = "fallbackBulkhead")
    public void bulkheadTestMethod() throws InterruptedException {
        permittedCount.incrementAndGet();
        Thread.sleep(100);
    }

    private void fallbackBulkhead(CallNotPermittedException ex) {
        rejectedCount.incrementAndGet();
        throw new ServiceException(ErrorCode.API_ERROR, ex);
    }

    public int getPermittedCount() {
        return permittedCount.get();
    }

    public int getRejectedCount() {
        return rejectedCount.get();
    }

    public void resetBulkheadCounters() {
        permittedCount.set(0);
        rejectedCount.set(0);
    }

    // ─── 타임리미터 테스트용 카운터 ─────────────────────────────────────
    @TimeLimiter(name = "youtubeService")
    public CompletionStage<String> timeoutTestMethod() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("timeoutTestMethod 시작 – 6초 대기");
                Thread.sleep(6_000);
                return "완료";
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @TimeLimiter(name = "youtubeService")
    public CompletionStage<String> timeoutSuccessMethod() {
        log.info("timeoutSuccessMethod 즉시 완료");
        return CompletableFuture.completedFuture("OK");
    }

}
