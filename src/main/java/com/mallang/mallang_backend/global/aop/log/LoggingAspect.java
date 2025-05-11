package com.mallang.mallang_backend.global.aop.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void allControllers() {}

    // @RestController 애노테이션 기반으로 적용
    @Around("allControllers()")
    public Object loggingParameter(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        /**
         * 예시:
         * [PaymentController#createPaymentRequest] parameter:
         *               CustomUserDetails(memberId=1, roleName=ROLE_STANDARD)
         *
         * - DTO 객체라면 @ToString 애노테이션 적용해야 제대로 된 값이 출력됩니다!
         * (안 하면 참조값 나옴)
         */
        Object[] args = joinPoint.getArgs(); // 파라미터 값
        for (Object arg : args) {
            log.info("[{}#{}] parameter: {}", className, methodName, arg);
        }

        Object result = joinPoint.proceed();

        // 반환값 로깅
        log.info("[{}#{}] return: {}", className, methodName, result);

        return result;
    }
}
