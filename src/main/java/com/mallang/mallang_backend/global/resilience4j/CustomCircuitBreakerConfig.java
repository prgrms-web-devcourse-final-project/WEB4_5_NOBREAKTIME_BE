package com.mallang.mallang_backend.global.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.micrometer.core.instrument.Metrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.resilience4j.circuitbreaker.CircuitBreaker.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomCircuitBreakerConfig {

    /**
     * 서킷 브레이커 이벤트 소비자 빈 등록
     * - 새로 생성되는 모든 서킷 브레이커에 자동으로 이벤트 리스너 등록
     */
    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
        return new CircuitBreakerEventConsumer();
    }

    /**
     * 서킷 브레이커 이벤트 소비자 구현
     */
    public class CircuitBreakerEventConsumer implements RegistryEventConsumer<CircuitBreaker> {

        // 예시: 서킷 브레이커 등록됨: oauthUserLoginService
        @Override
        public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> event) {
            CircuitBreaker circuitBreaker = event.getAddedEntry();
            registerEventListeners(circuitBreaker);
            log.info("서킷 브레이커 등록됨: {}", circuitBreaker.getName());
        }

        @Override
        public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> event) {
            log.info("서킷 브레이커 제거됨: {}", event.getRemovedEntry().getName());
        }

        @Override
        public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> event) {
            CircuitBreaker circuitBreaker = event.getNewEntry();
            registerEventListeners(circuitBreaker);
            log.info("서킷 브레이커 대체됨: {}", circuitBreaker.getName());
        }
    }

    /**
     * 서킷 브레이커에 이벤트 리스너 등록
     */
    private void registerEventListeners(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        try {
            // 각 이벤트 타입별 리스너 등록
            subscribeToStateTransitionEvents(circuitBreaker);
            subscribeToErrorEvents(circuitBreaker);
            subscribeToIgnoredErrorEvents(circuitBreaker);
            subscribeToSuccessEvents(circuitBreaker);
        } catch (Exception e) {
            log.error("서킷 브레이커 {} 이벤트 리스너 등록 실패", name, e);
        }
    }

    /**
     * 상태 변경 이벤트 구독
     */
    private void subscribeToStateTransitionEvents(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        circuitBreaker.getEventPublisher().onStateTransition(event -> {
            StateTransition transition = event.getStateTransition();
            String fromState = transition.getFromState().name();
            String toState = transition.getToState().name();

            // 메트릭 기록
            recordStateChangeMetric(name, fromState, toState);

            // 로그 기록
            log.warn("[{}] 상태 변경: {} → {}", name, fromState, toState);
        });
    }

    /**
     * 에러 이벤트 구독
     */
    private void subscribeToErrorEvents(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        circuitBreaker.getEventPublisher().onError(event -> {
            log.error("[{}] 에러: {}", name, event.getThrowable().getMessage());
        });
    }

    /**
     * 무시된 에러 이벤트 구독
     */
    private void subscribeToIgnoredErrorEvents(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        circuitBreaker.getEventPublisher().onIgnoredError(event -> {
            log.warn("[{}] 무시된 에러: {}", name, event.getThrowable().getMessage());
        });
    }

    /**
     * 성공 이벤트 구독
     */
    public void subscribeToSuccessEvents(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        circuitBreaker.getEventPublisher().onSuccess(event -> {
            log.debug("[{}] 성공 ({}ms)", name, event.getElapsedDuration().toMillis());
        });
    }

    /**
     * 상태 변경 메트릭 기록
     */
    private void recordStateChangeMetric(String name, String fromState, String toState) {
        Metrics.counter("circuit_breaker_state_change",
                        "name", name,
                        "from", fromState,
                        "to", toState)
                .increment();
    }
}
