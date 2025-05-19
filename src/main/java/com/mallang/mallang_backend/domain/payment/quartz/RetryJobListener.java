package com.mallang.mallang_backend.domain.payment.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class RetryJobListener implements JobListener {

    @Value("${quartz.retry.interval}")
    private int retryIntervalMinutes;

    @Override
    public String getName() {
        return "retryJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {
        if (jobException != null) {
            JobDataMap dataMap = context.getMergedJobDataMap();
            int maxRetry = dataMap.getInt("maxRetry");
            int currentRetry = dataMap.getIntValue("currentRetry");

            if (currentRetry < maxRetry) {
                scheduleRetry(context, currentRetry + 1);
                log.warn("Job 재시도 시도: {}/{}회", currentRetry + 1, maxRetry);
            }
        }
    }

    /**
     * 기존 트리거는 실행 후 자동으로 제거됨 (단발성 실행)
     * 새로운 트리거를 생성해서 값을 연동해 주는 방식으로 count 를 관리해야 한다
     */
    private void scheduleRetry(JobExecutionContext context,
                               int newRetryCount) {

        TriggerKey triggerKey = createTriggerKey(context, newRetryCount);
        try {
            Trigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(context.getJobDetail().getKey())
                    .startAt(nextFireTime())
                    .usingJobData("currentRetry", newRetryCount)
                    .build();
            log.info("Job: {} 재시도 트리거 생성: {}", context.getJobDetail().getKey(), newTrigger.getKey().getName());
            context.getScheduler().scheduleJob(newTrigger);
        } catch (SchedulerException e) {
            log.error("Job: {} 재시도 중 예외 발생", context.getJobDetail().getKey());
        }
    }

    private TriggerKey createTriggerKey(JobExecutionContext context, int newRetryCount) {
        return TriggerKey.triggerKey(
                context.getTrigger().getKey().getName() + "-RETRY-" + newRetryCount
        );
    }

    // 10분 간격 재시도 3회
    private Date nextFireTime() {
        return Date.from(
                Instant.now().plus(retryIntervalMinutes, ChronoUnit.MINUTES)
        );
    }
}
