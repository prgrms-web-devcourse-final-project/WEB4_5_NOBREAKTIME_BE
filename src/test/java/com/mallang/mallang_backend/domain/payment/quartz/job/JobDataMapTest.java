package com.mallang.mallang_backend.domain.payment.quartz.job;

import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.config.QuartzConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.doThrow;

@Slf4j
@SpringBootTest
@Import({QuartzConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 컨텍스트 재생성
public class JobDataMapTest {

    @Autowired
    private Scheduler scheduler;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Test
    @Transactional
    @DisplayName("JobDataMap의 값을 변경하는 테스트 - 실패 시 예외 발생, 재시도 값 누적")
    void JobDataMap_fail() throws Exception {
        //given: 예외 발생 시도
        doThrow(new RuntimeException("테스트 예외 발생"))
                .when(subscriptionService)
                .updateSubscriptionStatus();

        //when: JobDataMap 데이터 값 조회
        JobKey jobKey = JobKey.jobKey("subscriptionExpireJob", "DEFAULT");
        JobDataMap initialData = scheduler.getJobDetail(jobKey).getJobDataMap();

        // When: 작업 실행 후 재조회
        scheduler.triggerJob(jobKey);
        JobDataMap updatedData = scheduler.getJobDetail(jobKey).getJobDataMap();

        //then
        int currentRetry = SubscriptionExpireJob.JobDataUtils.getIntValue(initialData, "currentRetry", 0);
        int updatedRetry = SubscriptionExpireJob.JobDataUtils.getIntValue(updatedData, "currentRetry", 0);

        log.info("Initial Data: {}", currentRetry);
        log.info("Updated Data: {}", updatedRetry);
    }


}
