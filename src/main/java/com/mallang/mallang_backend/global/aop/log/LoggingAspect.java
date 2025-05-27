package com.mallang.mallang_backend.global.aop.log;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.exception.message.MessageService;
import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private final MessageService messageService;

    public LoggingAspect(MessageService messageService) {
        this.messageService = messageService;
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void allControllers() {}

    @Pointcut("execution(* com.mallang.mallang_backend.domain.payment.quartz.job..*(..))")
    public void paymentScheduler() {}

    // @RestController 애노테이션 기반으로 적용
    @Around("allControllers() || paymentScheduler()")
    public Object loggingParameter(ProceedingJoinPoint joinPoint) throws Throwable {

        // 요청 URI 확인
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String uri = request.getRequestURI();

            if ("/actuator/prometheus".equals(uri)) {
                return joinPoint.proceed(); // Prometheus 요청은 로깅/추적 제외
            }
        }

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        /**
         * 요청 파라미터 로그 예시:
         * <p>
         * [PaymentController#createPaymentRequest] parameter:
         *     CustomUserDetails(memberId=1, roleName=ROLE_STANDARD)
         *     PaymentRequestDto(amount=10000, productId=42)
         * <p>
         * ※ 주의:
         * - DTO나 커스텀 객체의 파라미터를 출력하려면 반드시 해당 클래스에
         *   `@ToString` 또는 `@Getter @Setter @ToString` 등의 Lombok 애노테이션이 있어야 합니다.
         *   그렇지 않으면 로그에는 다음처럼 객체 참조값만 출력됩니다:
         * <p>
         *     com.mallang.mallang_backend.domain.payment.PaymentRequestDto@1f6c5a8
         * <p>
         * - 민감 정보(비밀번호, 카드번호 등)는 `@ToString.Exclude`로 반드시 제외 처리하세요:
         *
         *     @ToString
         *     public class LoginRequest {
         *         private String email;
         *
         *         @ToString.Exclude
         *         private String password;
         *     }
         */
        Object[] args = joinPoint.getArgs(); // 파라미터 값
        for (Object arg : args) {
            log.info("[{}#{}] parameter: {}", className, methodName, arg);
        }

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            log.info("[{}#{}] return: {}", className, methodName, result);
            log.info("[{}#{}] executionTime: {}ms", className, methodName, end - start);
            return result;
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                String messageCode = ((ServiceException) e).getMessageCode();
                String message = messageService.getMessage(messageCode);
                log.error("[{}#{}] 예외 발생: {}", className, methodName, message);
            }
            log.error("[{}#{}] 예외 발생: {}", className, methodName, e.getMessage());
            Sentry.captureException(e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
