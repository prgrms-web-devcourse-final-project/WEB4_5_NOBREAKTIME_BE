package com.mallang.mallang_backend.global.aop.monitor;

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
public class MetricExecutionAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(measureExecutionTime)")
    public Object measure(ProceedingJoinPoint pjp, MeasureExecutionTime measureExecutionTime) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        Timer.Sample sample = Timer.start(meterRegistry);

        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("[@MeasureExecutionTime] {}#{} 실행 시간: {}ms", className, methodName, duration);
            sample.stop(Timer.builder("annotated_method_execution_seconds")
                    .description("메서드 실행 시간")
                    .tags("class", className, "method", methodName, "status", "success")
                    .register(meterRegistry));
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[@MeasureExecutionTime] {}#{} 실패 ({}ms)", className, methodName, duration);

            sample.stop(Timer.builder("annotated_method_execution_seconds")
                    .description("메서드 실행 시간")
                    .tags("class", className, "method", methodName, "status", "fail")
                    .register(meterRegistry));

            throw t;
        }
    }
}
