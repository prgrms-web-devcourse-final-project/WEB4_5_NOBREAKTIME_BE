package com.mallang.mallang_backend.global.aop.monitor;

import com.mallang.mallang_backend.global.dto.TokenUsageType;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExternalApiMonitoringAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(monitorExternalApi)")
    public Object monitor(ProceedingJoinPoint joinPoint, MonitorExternalApi monitorExternalApi) throws Throwable {
        String apiName = monitorExternalApi.name();
        TokenUsageType usageType = monitorExternalApi.usageType();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Timer.Sample sample = Timer.start(meterRegistry);
        boolean success = false;
        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed();
            success = true;
        } catch (Throwable t) {
            throwable = t;
        }

        // API 호출 카운트 증가
        meterRegistry.counter("external_api_call_total", "api", apiName).increment();

        // OpenAI인 경우 실제 토큰 사용량 측정
        if ("openai".equalsIgnoreCase(apiName)) {
            double tokensUsed = 0;
            if (result instanceof OpenAiResponse response && response.getUsage() != null) {
                tokensUsed = response.getUsage().getTotal_tokens();
            }
            recordTokenUsage(apiName, tokensUsed, "ACTUAL_USAGE");

        } else {
            // 그 외에는 usageType 기반으로 고정 차감
            recordTokenUsage(apiName, usageType);

            if (!success) {
                recordTokenUsage(apiName, TokenUsageType.FAILURE_PENALTY);
            }
        }

        // 타이머 기록
        sample.stop(Timer.builder("external_api_call_seconds")
                .tags("api", apiName,
                        "status", (success ? "success" : "fail"),
                        "class", className,
                        "method", methodName)
                .register(meterRegistry));

        if (throwable != null) throw throwable;
        return result;
    }

    private void recordTokenUsage(String apiName, TokenUsageType usageType) {
        meterRegistry.counter("external_api_token_usage_total",
                "api", apiName,
                "usageType", usageType.name()
        ).increment(usageType.getCost());
    }

    private void recordTokenUsage(String apiName, double cost, String usageTypeLabel) {
        meterRegistry.counter("external_api_token_usage_total",
                "api", apiName,
                "usageType", usageTypeLabel
        ).increment(cost);
    }
}