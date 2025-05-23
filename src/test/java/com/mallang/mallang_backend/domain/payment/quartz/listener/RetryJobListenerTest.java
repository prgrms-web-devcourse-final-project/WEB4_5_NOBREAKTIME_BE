package com.mallang.mallang_backend.domain.payment.quartz.listener;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.slack.SlackNotifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RetryJobListenerTest {


    @Test
    @DisplayName("슬랙 알림 전송 테스트")
    void t1() throws Exception {
        //given: 테스트 대상 객체 생성
        SlackNotifier notifier = mock(SlackNotifier.class);
        RetryJobListener listener = new RetryJobListener(notifier);

        // retryIntervalMinutes 필드 설정 (리플렉션 사용)
        ReflectionTestUtils.setField(listener, "retryIntervalMinutes", 10);

        // Mock 객체들 생성
        JobExecutionContext context = mock(JobExecutionContext.class);
        JobExecutionException exception = mock(JobExecutionException.class);
        Trigger trigger = mock(Trigger.class);
        JobDetail jobDetail = mock(JobDetail.class);
        Scheduler scheduler = mock(Scheduler.class);
        TriggerKey triggerKey = TriggerKey.triggerKey("testTrigger", "DEFAULT");

        // JobDataMap 설정 (재시도 로직 실행을 위한 필수 데이터)
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("maxRetry", 3);
        jobDataMap.put("currentRetry", 2); // 마지막 재시도로 설정

        // when: Mock 동작 설정
        when(context.getTrigger()).thenReturn(trigger);
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(context.getScheduler()).thenReturn(scheduler);
        when(context.getMergedJobDataMap()).thenReturn(jobDataMap); // 이 부분이 중요!
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("testJob"));
        when(trigger.getJobDataMap()).thenReturn(jobDataMap);
        when(trigger.getKey()).thenReturn(triggerKey);
        when(trigger.getScheduleBuilder())
                .thenAnswer(invocation -> SimpleScheduleBuilder.simpleSchedule());

        // 스케줄러에서 예외 발생하도록 설정
        SchedulerException fakeException = new SchedulerException("테스트용 예외");
        doThrow(fakeException).when(scheduler).scheduleJob(any(Trigger.class));

        try {
            listener.jobWasExecuted(context, exception);
        } catch (ServiceException e) {
            // 예외 발생은 정상
        }

        // then: 슬랙 알림이 호출되었는지 검증
        verify(notifier, times(1)).sendSlackNotification(
                eq("구독 자동 만료 실패 알림"),
                contains("테스트용 예외")
        );
    }

}