package com.mallang.mallang_backend.global.slack;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SlackNotificationAspect {

    private final SlackNotifier slackNotifier;

    /**
     * 사용 예시
     *  @SlackNotification(
     *         title = "[특별 알림]",
     *         message = "이 메서드는 위험합니다!"
     * ) << 자동으로 해당 메서드가 실행될 때 슬랙으로 알림이 갑니다
     *
     *     스케줄링 등록한 주문에 대해서 쉽게 판별하기 위해 만들었는데, 필요하신 분들 사용하세요!!!
     */
    @Before("@annotation(slackNotification)")
    public void before(JoinPoint joinPoint, SlackNotification slackNotification) throws Throwable {
        String baseTitle   = slackNotification.title();
        String baseMessage = slackNotification.message();
        String methodName  = joinPoint.getSignature().getName();

        // 기본 타이틀 적용
        String title = baseTitle;
        // 언어별 접두사 추가
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof JobExecutionContext) {
            JobExecutionContext context = (JobExecutionContext) args[0];
            String language = context.getMergedJobDataMap().getString("language");
            if ("ja".equalsIgnoreCase(language)) {
                title = "[JP] " + baseTitle;
            } else if ("en".equalsIgnoreCase(language)) {
                title = "[EN] " + baseTitle;
            }
        }

        String fullMessage = String.format("%s\n> 메서드: `%s()`", baseMessage, methodName);
        slackNotifier.sendSlackNotification(title, fullMessage);
    }
}
