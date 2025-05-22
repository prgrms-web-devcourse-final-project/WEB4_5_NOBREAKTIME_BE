package com.mallang.mallang_backend.global.resilience4j;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.mallang.mallang_backend.global.resilience4j.CustomCircuitBreakerConfig.CircuitBreakerEventConsumer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.resilience4j.code.TestService;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestService.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CustomCircuitBreakerConfigTest {

    @Autowired
    private TestService testService;

    @Autowired
    private CircuitBreakerRegistry registry;

    @Autowired
    private CircuitBreakerEventConsumer customConsumer;

    @Test
    @Disabled("검증 완료")
    @DisplayName("서킷 브레이커 테스트 - CLOSED -> OPEN")
    void t1() throws Exception {
        //given
        CircuitBreaker circuitBreaker = registry.circuitBreaker("oauthUserLoginService");

        // 초기 상태는 닫혀 있는 것이 맞음
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        log.info("circuitBreaker state: {}", circuitBreaker.getState());

        //when
        for (int i = 0; i < 21; i++) {
            assertThrows(ServiceException.class, () -> testService.testMethod());
        }

        //then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        log.info("circuitBreaker state: {}", circuitBreaker.getState());

        // 요청이 거부됨을 확인할 수 있음
        assertThrows(ServiceException.class, () -> testService.testMethod());
    }

    @Test
    @Disabled("검증 완료")
    @DisplayName("OPEN -> HALF-OPEN 상태 변경")
    void t2() throws Exception {
        //given
        CircuitBreaker circuitBreaker = registry.circuitBreaker("oauthUserLoginService");

        //when: state OPEN
        circuitBreaker.transitionToOpenState();
        log.info("circuitBreaker state: {}", circuitBreaker.getState());

        //then
        Thread.sleep(30000); // 30초 후

        assertThrows(ServiceException.class, () -> testService.testMethod());
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
        log.info("circuitBreaker state: {}", circuitBreaker.getState());
    }

    @Test
    @Disabled("검증 완료")
    @DisplayName("서킷 브레이커 적용 -> 호출 성공")
    void t3() throws Exception {
        //given
        CircuitBreaker circuitBreaker = registry.circuitBreaker("oauthUserLoginService");

        //when: 정상 비즈니스 로직 실행
        circuitBreaker.executeRunnable(() -> testService.successMethod());

        //then
        assertThat(circuitBreaker.getState()).isNotEqualTo(CircuitBreaker.State.OPEN);
        log.info("circuitBreaker state: {}", circuitBreaker.getState());
    }

    @Test
    @Disabled("검증 완료")
    @DisplayName("CircuitBreakerEventConsumer 빈 등록 테스트")
    void t4_eventConsumerBean() {
        // oauthUserLoginService 인스턴스 가져오기
        CircuitBreaker circuitBreaker = registry.circuitBreaker("oauthUserLoginService");

        // customConsumer 빈이 잘 등록되어 있는지 확인
        assertThat(customConsumer).isNotNull();
    }

    @Test
    @Disabled("검증 완료")
    @DisplayName("StateTransition 이벤트로 메트릭 기록 테스트")
    void t5() {
        // 메트릭 초기화
        Metrics.globalRegistry.clear();

        // given: 고유한 이름으로 CircuitBreaker 생성
        String name = "stateTestService";
        CircuitBreaker cb = registry.circuitBreaker(name);

        // when: CLOSED -> OPEN 상태 전환
        cb.transitionToOpenState();

        // then: 메트릭 카운터 증가 확인
        Counter counter = Metrics.globalRegistry
            .find("circuit_breaker_state_change")
            .tags("name", name, "from", "CLOSED", "to", "OPEN")
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    @Disabled("검증 완료")
    @DisplayName("EntryRemoved 이벤트 호출 테스트 - 제거")
    void t6() {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("oauthUserLoginService");

        // registry에서 제거
        Optional<CircuitBreaker> removed = registry.remove("oauthUserLoginService");

        // 제거가 성공적으로 이루어졌는지 확인
        assertThat(removed)
            .withFailMessage("Expected circuit breaker to be removed from registry")
            .isPresent();
    }

    @Test
    @Disabled("검증 완료")
    @DisplayName("EntryReplaced 이벤트 호출 테스트 - 대체")
    void t7() {
        // oauthUserLoginService 인스턴스 생성
        CircuitBreaker original = registry.circuitBreaker("oauthUserLoginService");

        // 같은 이름으로 다시 생성하여 대체 효과
        CircuitBreaker replaced = registry.circuitBreaker("oauthUserLoginService");

        // 새로 꺼낸 인스턴스 확인
        assertThat(replaced)
            .withFailMessage("Expected replaced instance to be not null")
            .isNotNull();
    }


    @Test
    @Disabled("검증 완료")
    @DisplayName("EntryReplacedEvent 직접 호출 테스트 - Mockito mock 사용")
    void t8_directOnEntryReplacedEvent() {
        // given: Mockito를 이용해 이벤트 객체를 모킹
        @SuppressWarnings("unchecked")
        EntryReplacedEvent<CircuitBreaker> mockEvent = mock(EntryReplacedEvent.class);
        CircuitBreaker cb = registry.circuitBreaker("oauthUserLoginService");
        when(mockEvent.getNewEntry()).thenReturn(cb);

        // when: 이벤트 핸들러 직접 호출
        customConsumer.onEntryReplacedEvent(mockEvent);

        // then: 예외 없이 실행되어야 함
        // 추가로 로그 호출 등 내부 메서드 실행 여부는 수동 확인 또는 별도 spy로 검증 가능
    }
}