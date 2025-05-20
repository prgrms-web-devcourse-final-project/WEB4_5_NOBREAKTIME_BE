package com.mallang.mallang_backend.domain.payment.quartz.listener;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

@Slf4j
public class LoggingJobListener implements JobListener {
    @Override
    public String getName() {
        return "LoggingJobListener";
    }

    /**
     * 실행 전 로깅
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        log.info("[Logging 1] Job: {} 실행 대기", context.getJobDetail().getKey());
    }

    /**
     * 실행 중 로깅
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        log.info("[Logging 2] Job: {} 실행 중", context.getJobDetail().getKey());
    }

    /**
     * 실행 후 로깅
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (jobException != null) {
            log.error("[Logging 3-error]Job: {} 실행 중 예외 발생", context.getJobDetail().getKey(), jobException);
            return;
        }

        log.info("[Logging 3-success] Job: {} 실행 완료", context.getJobDetail().getKey());
    }
}
