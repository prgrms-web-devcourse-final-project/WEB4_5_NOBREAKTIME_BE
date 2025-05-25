package com.mallang.mallang_backend.global.config;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

import javax.sql.DataSource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.mallang.mallang_backend.domain.payment.quartz.AutowiringSpringBeanJobFactory;
import com.mallang.mallang_backend.domain.payment.quartz.job.AutoBillingJob;
import com.mallang.mallang_backend.domain.payment.quartz.job.SubscriptionExpireJob;
import com.mallang.mallang_backend.domain.payment.quartz.listener.LoggingJobListener;
import com.mallang.mallang_backend.domain.payment.quartz.listener.RetryJobListener;
import com.mallang.mallang_backend.domain.payment.quartz.listener.RetryTriggerListener;
import com.mallang.mallang_backend.domain.video.video.cache.quartz.job.CacheSchedulerJob;
import com.mallang.mallang_backend.global.slack.SlackNotifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class QuartzConfig {

    @Value("${quartz.cron.subscription-status-check}")
    private String subscriptionStatusCheckCron;

    @Value("${quartz.cron.auto-billing}")
    private String autoBillingCron;

    @Value("${quartz.cron.cache-scheduler}")
    private String cacheSchedulerCron;

    private final DataSource dataSource;  // Spring Boot가 자동 구성한 DataSource

    public QuartzConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Bean
    public JobDetail subscriptionExpireJobDetail() {
        return JobBuilder.newJob(SubscriptionExpireJob.class)
                .withIdentity("subscriptionExpireJob", "DEFAULT")
                .usingJobData("maxRetry", 3)
                .storeDurably(true)
                .requestRecovery(true)
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
                .withIdentity("autoBillingJob", "DEFAULT")
                .usingJobData("maxRetry", 3)
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail cacheSchedulerJobDetailEn() {
        return JobBuilder.newJob(CacheSchedulerJob.class)
            .withIdentity("cacheSchedulerJobEn", "VIDEO_CACHE")
            .usingJobData("q", "")
            .usingJobData("category", "")
            .usingJobData("language", "en")
            .usingJobData("fetchSize", CACHE_SCHEDULER_FETCH_SIZE)
            .storeDurably()
            .requestRecovery(true)
            .build();
    }

    @Bean
    public JobDetail cacheSchedulerJobDetailJp() {
        return JobBuilder.newJob(CacheSchedulerJob.class)
            .withIdentity("cacheSchedulerJobJp", "VIDEO_CACHE")
            .usingJobData("q", "")
            .usingJobData("category", "")
            .usingJobData("language", "jp")
            .usingJobData("fetchSize", CACHE_SCHEDULER_FETCH_SIZE)
            .storeDurably()
            .requestRecovery(true)
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
                .withIdentity("subscriptionExpireTrigger", "DEFAULT")
                .usingJobData("currentRetry", 0)
                .withSchedule(CronScheduleBuilder.cronSchedule(subscriptionStatusCheckCron))
                .build();
    }

    @Bean
    public Trigger autoBillingTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(autoBillingJobDetail())
                .withIdentity("autoBillingTrigger", "DEFAULT")
                .withSchedule(CronScheduleBuilder.cronSchedule(autoBillingCron)) // 매일 00:00 실행
                .build();
    }

    @Bean
    public Trigger cacheSchedulerTriggerEn() {
        return TriggerBuilder.newTrigger()
            .forJob(cacheSchedulerJobDetailEn())
            .withIdentity("cacheSchedulerTriggerEn", "VIDEO_CACHE")
            .withSchedule(CronScheduleBuilder.cronSchedule(cacheSchedulerCron)) // 매일 3시,4시
            .build();
    }

    @Bean
    public Trigger cacheSchedulerTriggerJp() {
        return TriggerBuilder.newTrigger()
            .forJob(cacheSchedulerJobDetailJp())
            .withIdentity("cacheSchedulerTriggerJp", "VIDEO_CACHE")
            .withSchedule(CronScheduleBuilder.cronSchedule(cacheSchedulerCron)) // 매일 3시,4시
            .build();
    }

    @Bean
    public LoggingJobListener loggingJobListener() {
        log.info(">>>> loggingJobListener 생성됨 <<<<");
        return new LoggingJobListener();
    }

    @Bean
    public RetryJobListener retryJobListener() {
        log.info(">>>> retryJobListener 생성됨 <<<<");
        return new RetryJobListener(new SlackNotifier());
    }

    @Bean
    public RetryTriggerListener retryTriggerListener() {
        log.info(">>>> retryTriggerListener 생성됨 <<<<");
        return new RetryTriggerListener();
    }

    /**
     * 스케줄러 생성 후 추가 커스터마이징을 위한 콜백 인터페이스
     * 전역 리스너(Trigger/JobListener) 설정에 주로 사용
     */
    @Bean
    public SchedulerFactoryBeanCustomizer schedulerCustomizer() {
        return schedulerFactoryBean -> {
            log.info(">>>> 스케줄러 커스터마이저 적용됨 <<<<");
            schedulerFactoryBean.setGlobalTriggerListeners(retryTriggerListener());
            schedulerFactoryBean.setGlobalJobListeners(loggingJobListener(), retryJobListener());
        };
    }

    @Bean
    public JobFactory autowiringSpringBeanJobFactory() {
        return new AutowiringSpringBeanJobFactory();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(autowiringSpringBeanJobFactory());
        factory.setJobDetails(subscriptionExpireJobDetail(), autoBillingJobDetail(), cacheSchedulerJobDetailEn(), cacheSchedulerJobDetailJp());
        factory.setTriggers(subscriptionExpireTrigger(), autoBillingTrigger(), cacheSchedulerTriggerEn(), cacheSchedulerTriggerJp());
        factory.setGlobalJobListeners(loggingJobListener(), retryJobListener());
        factory.setGlobalTriggerListeners(retryTriggerListener());
        factory.setDataSource(dataSource);
        return factory;
    }
}
