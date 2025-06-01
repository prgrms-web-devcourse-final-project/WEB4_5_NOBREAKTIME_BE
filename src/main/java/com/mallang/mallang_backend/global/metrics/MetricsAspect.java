package com.mallang.mallang_backend.global.metrics;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.exception.message.MessageService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAspect {

    private final MeterRegistry meterRegistry;
    private final MessageService messageService;

    @Around("@annotation(Monitor)")
    public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        // 메트릭 기본 정보 설정
        MetricInfo metricInfo = extractMetricInfo(joinPoint);

        // 타이머 시작
        Timer.Sample sample = startTimer();

        try {
            Object result = joinPoint.proceed(); // 타겟 실행

            incrementSuccessCounter(metricInfo); // 성공 시 카운터 증가
            return result;
        } catch (Exception e) {
            handleFailure(metricInfo, e);
            throw e;
        } finally {
            recordExecutionTime(metricInfo, sample); // 실행 시간 기록
        }
    }

    /**
     * 메트릭 정보 추출
     */
    private MetricInfo extractMetricInfo(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String metricName = getMetricName(joinPoint);
        return new MetricInfo(className, methodName, metricName);
    }

    private String getMetricName(ProceedingJoinPoint joinPoint) {
        Monitor monitor = ((MethodSignature) joinPoint.getSignature())
                .getMethod()
                .getAnnotation(Monitor.class);

        return monitor.name().isEmpty() ?
                "custom_metric" : monitor.name(); // 기본값 또는 커스텀 이름으로 적용할 수 있도록
    }

    /**
     * 타이머 시작
     */
    private Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 성공 카운터 증가
     */
    private void incrementSuccessCounter(MetricInfo metricInfo) {
        Tags successTags = buildSuccessTags(metricInfo.className(), metricInfo.methodName());
        Counter counter = Counter.builder(metricInfo.metricName() + "_total")
                .tags(successTags)
                .register(meterRegistry);
        counter.increment();
    }

    /**
     * 실패 처리
     */
    private void handleFailure(MetricInfo metricInfo, Exception e) {
        Tags errorTags = buildErrorTags(metricInfo.className(), metricInfo.methodName(), e);
        Counter counter = Counter.builder(metricInfo.metricName() + "_total")
                .tags(errorTags)
                .register(meterRegistry);
        counter.increment();
    }

    /**
     * 실행 시간 기록
     */
    private void recordExecutionTime(MetricInfo metricInfo, Timer.Sample sample) {
        Tags timeTags = buildTimeTags(metricInfo.className(), metricInfo.methodName());
        Timer timer = Timer.builder(metricInfo.metricName() + "_duration")
                .tags(timeTags)
                .register(meterRegistry);
        sample.stop(timer);
    }

    // === 태그 빌더 메서드 === //

    private Tags buildSuccessTags(String className, String methodName) {
        return Tags.of(
                "status", "success",
                "class", className,
                "method", methodName,
                "txId", MDC.get("transactionId")
        );
    }

    private Tags buildErrorTags(String className, String methodName, Exception e) {
        return Tags.of(
                "status", "fail",
                "class", className,
                "method", methodName,
                "message", getErrorMessage(e),
                "txId", MDC.get("transactionId")
        );
    }

    private String getErrorMessage(Exception e) {
        return (e instanceof ServiceException)
                ? messageService.getMessage(((ServiceException) e).getMessageCode())
                : e.getMessage();
    }

    private Tags buildTimeTags(String className, String methodName) {
        return Tags.of(
                "class", className,
                "method", methodName
        );
    }

    // === 메트릭 정보 전용 레코드 === //
    private record MetricInfo(
            String className,
            String methodName,
            String metricName
    ) {}
}
