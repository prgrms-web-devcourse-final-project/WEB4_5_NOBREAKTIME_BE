package com.mallang.mallang_backend.global.aop.monitor;

import com.mallang.mallang_backend.global.dto.TokenUsageType;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@DisplayName("AOP Aspect 통합 테스트")
@Import({
        ExternalApiMonitoringAspectTest.DummyMonitoredService.class,
        ExternalApiMonitoringAspectTest.MetricTestService.class
})
class ExternalApiMonitoringAspectTest {

    @Autowired
    DummyMonitoredService dummyMonitoredService;

    @Autowired
    MetricTestService metricTestService;

    @Test
    @DisplayName("OpenAI 응답 기반 AOP 적용 테스트")
    void monitorOpenAiApiResponse() {
        OpenAiResponse result = dummyMonitoredService.callOpenAi();
        assertNotNull(result);
        assertEquals(123, result.getUsage().getTotal_tokens());
    }

    @Test
    @DisplayName("@MeasureExecutionTime AOP 적용 테스트")
    void measureExecutionTimeTest() {
        String result = metricTestService.process();
        assertEquals("done", result);
    }

    @Test
    @DisplayName("예외 발생 시 ExternalApiMonitoringAspect 반영 테스트")
    void monitorApiFailure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dummyMonitoredService.callFailingApi();
        });
        assertEquals("API 호출 실패", exception.getMessage());
    }

    @Test
    @DisplayName("예외 발생 시 @MeasureExecutionTime AOP 반영 테스트")
    void measureExecutionTimeFailureTest() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            metricTestService.fail();
        });
        assertEquals("강제 실패", exception.getMessage());
    }

    @Service
    static class DummyMonitoredService {

        @MonitorExternalApi(name = "openai", usageType = TokenUsageType.REQUEST)
        public OpenAiResponse callOpenAi() {
            log.info("OpenAI API 호출 테스트 메서드 실행 중");
            OpenAiResponse.Usage usage = new OpenAiResponse.Usage();
            usage.setTotal_tokens(123);
            return new OpenAiResponse(null, usage);
        }

        @MonitorExternalApi(name = "externalService", usageType = TokenUsageType.FAILURE_PENALTY)
        public String callFailingApi() {
            log.info("실패하는 외부 API 호출 시도");
            throw new RuntimeException("API 호출 실패");
        }
    }

    @Service
    static class MetricTestService {

        @MeasureExecutionTime
        public String process() {
            log.info("MetricTestService.process() 호출됨");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "done";
        }

        @MeasureExecutionTime
        public String fail() {
            log.info("MetricTestService.fail() 예외 발생 예정");
            throw new RuntimeException("강제 실패");
        }
    }
}