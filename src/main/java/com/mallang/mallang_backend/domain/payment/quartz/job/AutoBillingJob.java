package com.mallang.mallang_backend.domain.payment.quartz.job;

import com.mallang.mallang_backend.domain.payment.quartz.service.AutoBillingService;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution // 중복 실행 방지
@PersistJobDataAfterExecution // 영속성
public class AutoBillingJob implements Job {

    private final AutoBillingService autoBillingService;

    @Override
    @TimeTrace
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        int currentRetry = dataMap.getIntValue("currentRetryCount");

        log.info("[최근 {} 재시도 횟수]: {}", context.getJobDetail().getKey(), currentRetry);
        try {
            autoBillingService.executeAutoBilling();
        } catch (Exception e) {
            throw new JobExecutionException(e, false);
        }
    }
}
