package com.mallang.mallang_backend.global.config;

import com.mallang.mallang_backend.domain.payment.quartz.*;
import com.mallang.mallang_backend.domain.payment.quartz.job.AutoBillingJob;
import com.mallang.mallang_backend.domain.payment.quartz.job.SubscriptionExpireJob;
import com.mallang.mallang_backend.domain.payment.quartz.listener.LoggingJobListener;
import com.mallang.mallang_backend.domain.payment.quartz.listener.RetryJobListener;
import com.mallang.mallang_backend.domain.payment.quartz.listener.RetryTriggerListener;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class QuartzConfig {

    @Value("${quartz.cron.subscription-status-check}")
    private String subscriptionStatusCheckCron;


    @Value("${quartz.cron.auto-billing}")
    private String autoBillingCron;

    @Bean
    public JobDetail subscriptionExpireJobDetail() {
        return JobBuilder.newJob(SubscriptionExpireJob.class)
                .withIdentity("subscriptionExpireJob", "subscriptionGroup")
                .usingJobData("maxRetry", 3)
                .usingJobData("currentRetry", 0)
                .storeDurably()
                .build();
    }

    /**
     * JobDetail의 JobDataMap: Job 자체에 대한 공통 정보
     * 여러 트리거가 같은 JobDetail을 쓸 수 있으므로,
     * 모든 트리거가 공유해야 하는 값(예: maxRetry, jobType, ownerEmail 등)을 저장하는 데 적합
     */
    @Bean
    public JobDetail autoBillingJobDetail() {
        return JobBuilder.newJob(AutoBillingJob.class)
                .withIdentity("autoBillingJob", "PaymentGroup")
                .usingJobData("maxRetry", 3)
                .storeDurably()
                .build();
    }

    /**
     * Trigger의 JobDataMap: 트리거(실행 단위)마다 독립적인 값을 저장
     * 재시도 카운트(currentRetry)처럼 실행마다 바뀌는 값을 저장하는 데 적합
     * 트리거가 새로 만들어질 때마다 그 트리거의 JobDataMap에 값을 넣어주면, 실행 시점에 정확한 값을 사용할 수 있음
     */
    @Bean
    public Trigger subscriptionExpireTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(subscriptionExpireJobDetail())
                .withIdentity("subscriptionExpireTrigger", "subscriptionGroup")
                .usingJobData("currentRetry", 0)
                .withSchedule(CronScheduleBuilder.cronSchedule(subscriptionStatusCheckCron)) // 매일 00:05 실행
                .build();
    }

    @Bean
    public Trigger autoBillingTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(autoBillingJobDetail())
                .withIdentity("autoBillingTrigger", "PaymentGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule(autoBillingCron)) // 매일 00:00 실행
                .build();
    }

    /**
     * 커스텀 설정
     * Job 의존성 주입, 리스너 등록(작업 실행 전/후 로깅, 에러 알림) 등
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            @Qualifier("quartzDataSource") DataSource quartzDataSource,
            RetryJobListener retryJobListener,
            LoggingJobListener loggingJobListener,
            RetryTriggerListener retryTriggerListener) {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(quartzDataSource);
        factory.setJobFactory(new AutowiringSpringBeanJobFactory()); // 커스텀 팩토리 설정
        factory.setJobDetails(subscriptionExpireJobDetail(), autoBillingJobDetail());
        factory.setTriggers(subscriptionExpireTrigger(), autoBillingTrigger());
        factory.setGlobalJobListeners(retryJobListener, loggingJobListener); // 글로벌 리스너 등록
        factory.setGlobalTriggerListeners(retryTriggerListener);
        return factory;
    }
}
