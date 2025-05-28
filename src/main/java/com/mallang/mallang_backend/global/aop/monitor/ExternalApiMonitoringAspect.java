package com.mallang.mallang_backend.global.aop.monitor;

import com.mallang.mallang_backend.global.aop.monitor.calaulator.OpenAiTokenCalculator;
import com.mallang.mallang_backend.global.aop.monitor.calaulator.TokenUsageCalculator;
import com.mallang.mallang_backend.global.aop.monitor.calaulator.registry.TokenCalculatorRegistry;
import com.mallang.mallang_backend.global.dto.TokenUsageType;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static io.micrometer.core.instrument.Timer.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExternalApiMonitoringAspect {

    private final MeterRegistry meterRegistry;
    private final TokenCalculatorRegistry calculatorRegistry;


    /**
     * 외부 API 호출 모니터링을 위한 AOP Aspect
     * - 실행 시간 측정
     * - 호출 횟수 카운트
     * - 토큰 사용량 추적
     */
    @Around("@annotation(monitorExternalApi)")
    public Object monitor(ProceedingJoinPoint joinPoint, MonitorExternalApi monitorExternalApi) throws Throwable {
        // 기본 정보 추출
        String apiName = monitorExternalApi.name(); // API 서비스명(ex: openai)
        TokenUsageType usageType = monitorExternalApi.usageType(); // 토큰 사용 유형
        String className = joinPoint.getTarget().getClass().getSimpleName(); // 호출 클래스
        String methodName = joinPoint.getSignature().getName(); // 호출 메서드

        Sample sample = start(meterRegistry); // 실행 시간 측정 시작
        boolean success = false;
        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed(); // 실제 타겟 메서드 실행
            success = true;
        } catch (Throwable t) {
            throwable = t; // 예외 발생 시 throwable 저장
        }

        // API 호출 카운트 증가
        meterRegistry.counter("external_api_call_total", "api", apiName).increment();

        // 2. 토큰 사용량 기록 (API별 차별화된 처리)
        handleTokenUsage(apiName, result, success, usageType);

        // 3. 실행 시간 기록 (공통)
        recordExecutionTime(sample, apiName, success, className, methodName);

        if (throwable != null) throw throwable;
        return result;
    }

    private void recordExecutionTime(Sample sample,
                                     String apiName,
                                     boolean success,
                                     String className,
                                     String methodName
    ) {
        sample.stop(builder("external_api_call_seconds")
                .tags("api", apiName,
                        "status", (success ? "success" : "fail"),
                        "class", className,
                        "method", methodName)
                .register(meterRegistry));
    }

    private void handleTokenUsage(String apiName, Object result, boolean success, TokenUsageType usageType) {
        TokenUsageCalculator calculator = calculatorRegistry.getCalculator(apiName);
        double tokensUsed = calculator.calculate(apiName, result, success, usageType);

        String usageTypeLabel = (calculator instanceof OpenAiTokenCalculator)
                ? "ACTUAL_USAGE"
                : usageType.name();

        recordTokenUsage(apiName, tokensUsed, usageTypeLabel);
    }

    private void recordTokenUsage(String apiName, double cost, String usageTypeLabel) {
        meterRegistry.counter("external_api_token_usage_total",
                "api", apiName,
                "usageType", usageTypeLabel
        ).increment(cost);
    }
}