package com.mallang.mallang_backend.domain.payment.quartz.listener;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.slack.SlackNotifier;
import com.mallang.mallang_backend.global.util.job.JobDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.mallang.mallang_backend.global.exception.ErrorCode.SUBSCRIPTION_STATUS_UPDATE_FAILED;

@Slf4j
public class RetryJobListener implements JobListener {

    @Value("${quartz.retry.interval}")
    private int retryIntervalMinutes;

    private final SlackNotifier notifier;

    public RetryJobListener(SlackNotifier notifier) {
        this.notifier = notifier;
    }

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
        log.info("재시도 로직 실행 검토");

        // 조건 1: Job 실행 중 예외가 발생했는가?
        if (jobException != null) {
            JobDataMap dataMap = context.getMergedJobDataMap();
            int maxRetry = JobDataUtils.getIntValue(dataMap, "maxRetry", 3);
            int currentRetry = JobDataUtils.getIntValue(dataMap, "currentRetry", 0);

            // 조건 2: 재시도 횟수가 남았는가?
            if (currentRetry < maxRetry) {
                scheduleRetry(context, currentRetry + 1); // 재시도 로직 실행 (새로운 트리거 생성)
                log.warn("Job 재시도 시도: {}/{}회", currentRetry + 1, maxRetry);
            } else {
                log.error("최대 재시도 횟수 초과: {}", jobException.getMessage());
            }
        }
    }

    /**
     * 기존 트리거는 실행 후 자동으로 제거됨 (단발성 실행)
     * 새로운 트리거를 생성해서 값을 연동해 주는 방식으로 count 를 관리해야 한다
     */
    private void scheduleRetry(JobExecutionContext context,
                               int newRetryCount) {
        try {
            // 1. 기존 트리거 조회
            Trigger oldTrigger = context.getTrigger();

            // 2. 기존 JobDataMap 복사
            JobDataMap newJobDataMap = new JobDataMap(oldTrigger.getJobDataMap());
            newJobDataMap.put("currentRetry", newRetryCount);  // 재시도 횟수 업데이트

            // 3. 새로운 트리거 생성 (기존 트리거 설정 유지)
            Trigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(createTriggerKey(context, newRetryCount).getName(), "DEFAULT")
                    .forJob(context.getJobDetail().getKey())
                    .startAt(nextFireTime())
                    .usingJobData(newJobDataMap)  // 복사된 JobDataMap 설정
                    .withSchedule(oldTrigger.getScheduleBuilder())  // 기존 스케줄 유지 (예: CronSchedule)
                    .build();

            // 4. 새로운 트리거 등록
            context.getScheduler().scheduleJob(newTrigger);
            log.info("[재시도] Job: {} 재시도 트리거 생성: {}", context.getJobDetail().getKey(), newTrigger.getKey().getName());
            log.info("[재시도] 새로운 JobDataMap: {}", newJobDataMap.getWrappedMap());
        } catch (SchedulerException e) {
            log.error("Job: {} 재시도 중 예외 발생: {}", context.getJobDetail().getKey(), e.getMessage(), e);
            notifier.sendSlackNotification(
                    "구독 자동 만료 실패 알림",
                    "스케줄러 자동 만료 처리 중 오류가 발생했습니다.\n> 에러 내용: `" + e.getMessage() + "`"
            );
            throw new ServiceException(SUBSCRIPTION_STATUS_UPDATE_FAILED, e);
        }
    }

    private TriggerKey createTriggerKey(JobExecutionContext context, int newRetryCount) {
        return TriggerKey.triggerKey(
                context.getTrigger().getKey().getName() + "-RETRY-" + newRetryCount,
                context.getTrigger().getKey().getGroup()
        );
    }

    // 10분 간격 재시도 3회
    private Date nextFireTime() {
        return Date.from(
                Instant.now().plus(retryIntervalMinutes, ChronoUnit.MINUTES)
        );
    }
}
