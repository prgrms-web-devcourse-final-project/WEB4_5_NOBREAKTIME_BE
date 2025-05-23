package com.mallang.mallang_backend.global.slack;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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
        String title = slackNotification.title();
        String message = slackNotification.message();
        String methodName = joinPoint.getSignature().getName();

        String fullMessage = String.format("%s\n> 메서드: `%s()`", message, methodName);

        slackNotifier.sendSlackNotification(title, fullMessage);
    }
}
