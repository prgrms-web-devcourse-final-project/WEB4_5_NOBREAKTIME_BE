package com.mallang.mallang_backend.domain.payment.quartz.job;

import com.mallang.mallang_backend.domain.payment.quartz.service.AutoBillingService;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import com.mallang.mallang_backend.global.slack.SlackNotification;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component // 스프링 빈으로 등록 (Job 등록 시 자동으로 등록 됨)
@DisallowConcurrentExecution // 중복 실행 방지
@PersistJobDataAfterExecution // 영속성
public class AutoBillingJob implements Job {

    private final AutoBillingService autoBillingService;

    public AutoBillingJob(AutoBillingService autoBillingService) {
        this.autoBillingService = autoBillingService;
    }

    @Override
    @TimeTrace
    @SlackNotification(
            title = "자동 결제",
            message = "현재 자동 결제 스케줄링이 실행 준비 중입니다."
    )
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        int currentRetry = context.getTrigger()
                .getJobDataMap()
                .getInt("currentRetry");

        log.info("[최근 {} 재시도 횟수]: {}", context.getJobDetail().getKey(), currentRetry);
        try {
            autoBillingService.executeAutoBilling();
            dataMap.put("currentRetry", 0);
        } catch (Exception e) {
            dataMap.put("currentRetry", currentRetry + 1);
            throw new JobExecutionException(e, false);
        }
    }
}
