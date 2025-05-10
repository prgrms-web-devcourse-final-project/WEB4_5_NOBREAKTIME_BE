package com.mallang.mallang_backend.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("@annotation(com.mallang.mallang_backend.global.aop.TimeTrace)")
    public Object traceTime(ProceedingJoinPoint joinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            log.info(" {} - 실행 시간 : {} ms",
                    joinPoint.getSignature().toShortString(),
                    stopWatch.getTotalTimeMillis());
        }
    }

}
