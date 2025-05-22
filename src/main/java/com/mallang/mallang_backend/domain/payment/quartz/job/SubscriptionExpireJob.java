package com.mallang.mallang_backend.domain.payment.quartz.job;

import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import com.mallang.mallang_backend.global.util.job.JobDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 자동으로 구독이 ACTIVE -> EXPIRED 될 수 있도록 변경이 필요
 */
@Slf4j
@Component
@DisallowConcurrentExecution // 중복 실행 방지
@PersistJobDataAfterExecution // 영속성
public class SubscriptionExpireJob implements Job {
    public static final List<Long> executionTimes = new ArrayList<>();
    public static final AtomicInteger executionCount = new AtomicInteger(0);

    private final SubscriptionService subscriptionService;

    public SubscriptionExpireJob(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService; // Lombok 의존성 주입이 안 됨, 명시적 주입이 필요
    }

    @Override
    @TimeTrace
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long now = System.currentTimeMillis();
        executionTimes.add(now);
        int count = executionCount.incrementAndGet();
        log.info("Job 실행 시작: {} (실행 카운트: {})", now, count);

        JobDataMap dataMap = context.getMergedJobDataMap();
        int currentRetry = JobDataUtils.getIntValue(dataMap, "currentRetry", 0);

        log.info("[최근 {} 재시도 횟수]: {}", context.getJobDetail().getKey(), currentRetry);
        try {
            subscriptionService.updateSubscriptionStatus();
            dataMap.put("currentRetry", 0); // 성공 시 재시도 횟수 초기화
        } catch (Exception e) {
            int maxRetry = JobDataUtils.getIntValue(dataMap, "maxRetry", 0);
            log.error("구독 변경 Job 실행 중 예외 발생 - 재시도 횟수: {}/{}",
                    currentRetry, maxRetry, e);

            throw new JobExecutionException(e, false); // Listener 로 예외 전파
        }
    }
}


