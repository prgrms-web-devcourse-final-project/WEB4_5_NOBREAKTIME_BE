package com.mallang.mallang_backend.global.resilience4j;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.resilience4j.code.TestService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestService.class})
class CustomCircuitBreakerConfigTest {

    @Autowired
    private TestService testService;

    @Autowired
    private CircuitBreakerRegistry registry;

    @BeforeEach
    void resetCircuitBreaker() {
        registry.remove("oauthUserLoginService");
    }

    @Test
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
    @DisplayName("서킷 브레이커 적용 -> 호출 성공")
    void t3() throws Exception {
        //given
        CircuitBreaker circuitBreaker = registry.circuitBreaker("oauthUserLoginService");

        //when: 정상 비즈니스 로직 실행
        circuitBreaker.executeRunnable(() -> testService.successMethod());

        //then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        log.info("circuitBreaker state: {}", circuitBreaker.getState());
    }
}