package com.mallang.mallang_backend.domain.payment.quartz.job;

import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.entity.SubscriptionStatus;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.global.config.QuartzConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
@Import({QuartzConfig.class})
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 컨텍스트 재생성
public class QuartzAutoJobIntegrationTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobDetail subscriptionExpireJobDetail;

    @Autowired
    private Trigger subscriptionExpireTrigger;

    @Autowired
    private JobDetail autoBillingJobDetail;

    @Autowired
    private Trigger autoBillingTrigger;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @AfterEach
    void tearDown() throws SchedulerException {
        scheduler.clear(); // 테스트 간 상태 격리
    }

    // CronTrigger의 getNextFireTime() 메서드로 다음 실행 시간 확인
    // 동시 실행 방지 검증
    // JobListener/TriggerListener의 콜백 메서드 호출 확인

    @Test
    @DisplayName("쿼츠 스케줄러 통합 테스트 - 초기 실행 확인")
    void t0() throws Exception {
        //given
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                // JobDetail 정보
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                System.out.println("Job Name: " + jobName + ", Group: " + jobGroup);
                System.out.println("JobDetail: " + jobDetail);

                // 이 Job에 연결된 Trigger 정보
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    System.out.println("  Trigger: " + trigger.getKey() + ", Next Fire Time: " + trigger.getNextFireTime());
                }
            }
        }
        List<JobListener> listeners = scheduler.getListenerManager().getJobListeners();
        listeners.forEach(listener ->
                log.info("등록된 리스너: {}", listener.getName())
        );

        scheduler.getJobDetail(subscriptionExpireJobDetail.getKey());
        scheduler.getTrigger(subscriptionExpireTrigger.getKey());

        assertThat(scheduler.getJobDetail(subscriptionExpireJobDetail.getKey())).isNotNull();
        assertThat(scheduler.getTrigger(subscriptionExpireTrigger.getKey())).isNotNull();

        scheduler.getJobDetail(autoBillingJobDetail.getKey());
        scheduler.getTrigger(autoBillingTrigger.getKey());

        assertThat(scheduler.getJobDetail(autoBillingJobDetail.getKey())).isNotNull();
        assertThat(scheduler.getTrigger(autoBillingTrigger.getKey())).isNotNull();
    }

    @Test
    @DisplayName("쿼츠 스케줄러 통합 테스트 - 구독상태가 EXPIRED로 변경되는지 확인")
    void t1(CapturedOutput output) throws Exception {
        // given
        if (!scheduler.isStarted()) {
            scheduler.start();  // 명시적으로 시작
            log.info("쿼츠 스케줄러 시작");
        }

        JobKey jobKey = JobKey.jobKey("subscriptionExpireJob", "DEFAULT");

        // When: 작업 실행 후 재조회
        scheduler.triggerJob(jobKey);

        // when: Job 강제 실행
        Thread.sleep(2000);
        Subscription subscription = subscriptionRepository.findById(6L).get();
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);

        assertThat(output.getOut())
                .contains("[Logging 1] Job: DEFAULT.subscriptionExpireJob 실행 대기")
                .contains("[Logging 3-success] Job: DEFAULT.subscriptionExpireJob 실행 완료")
                .contains("[구독만료성공]");
    }

    @Test
    @DisplayName("중복 실행 방지 애노테이션 동작 확인")
    void t2(CapturedOutput output) throws Exception {
        //given: @DisallowConcurrentExecution 적용된 Job
        JobKey jobKey = JobKey.jobKey("subscriptionExpireJob", "DEFAULT");

        //when: 동시 실행 시도 -> 대기 상태
        for(int i = 0; i < 3; i++) {
            scheduler.triggerJob(jobKey);
        }
        Thread.sleep(3000); // Job이 실행될 시간 대기

        // then: 실행 시각 간의 차이가 겹치지 않는지 확인
        List<Long> times = SubscriptionExpireJob.executionTimes;
        for (int i = 1; i < times.size(); i++) {
            assertThat(times.get(i) - times.get(i - 1)).isGreaterThan(0);
            log.info("시간 차이: {}", times.get(i) - times.get(i - 1));
        }

        assertThat(output.getOut()).contains("(실행 카운트: 3)");
    }

    @Test
    @Transactional
    @DisplayName("JobDataMap의 값을 변경하는 테스트 - 성공 시 0으로 초기화")
    void JobDataMap_success() throws Exception {
        // Given: 초기 값 설정
        JobKey jobKey = JobKey.jobKey("subscriptionExpireJob", "DEFAULT");
        JobDataMap initialData = scheduler.getJobDetail(jobKey).getJobDataMap();

        // When: 작업 실행 후 재조회
        scheduler.triggerJob(jobKey);
        JobDataMap updatedData = scheduler.getJobDetail(jobKey).getJobDataMap();

        int currentRetry = SubscriptionExpireJob.JobDataUtils.getIntValue(initialData, "currentRetry", 0);
        int updatedRetry = SubscriptionExpireJob.JobDataUtils.getIntValue(updatedData, "currentRetry", 0);
        // Then: 값 변경 확인
        log.info("Initial Data: {}", currentRetry);
        log.info("Updated Data: {}", updatedRetry);

        assertThat(updatedRetry).isEqualTo(0);
    }
}