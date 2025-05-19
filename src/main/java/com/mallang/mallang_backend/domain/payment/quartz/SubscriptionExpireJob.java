package com.mallang.mallang_backend.domain.payment.quartz;

import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * 자동으로 구독이 ACTIVE -> EXPIRED 될 수 있도록 변경이 필요
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution // 중복 실행 방지
@PersistJobDataAfterExecution // 영속성
public class SubscriptionExpireJob implements Job {

    private final SubscriptionService subscriptionService;

    @Override
    @TimeTrace
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        int currentRetry = context.getTrigger()
                .getJobDataMap()
                .getInt("currentRetry");

        log.info("[최근 {} 재시도 횟수]: {}", context.getJobDetail().getKey(), currentRetry);
        try {
            subscriptionService.updateSubscriptionStatus();
            dataMap.put("currentRetry", 0);
        } catch (Exception e) {
            dataMap.put("currentRetry", currentRetry + 1);
            throw new JobExecutionException(e, false);
        }
    }
}


