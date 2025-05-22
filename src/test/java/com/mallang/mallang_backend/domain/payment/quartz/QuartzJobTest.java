package com.mallang.mallang_backend.domain.payment.quartz;

import com.mallang.mallang_backend.domain.payment.quartz.job.SubscriptionExpireJob;
import com.mallang.mallang_backend.domain.payment.quartz.listener.RetryJobListener;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.mockito.Mockito;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 컨텍스트 재생성
class QuartzJobTest {

    @MockitoBean
    private LoggingListener loggingListener;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Autowired
    private RetryJobListener JobListener;

    @Autowired
    private JobDetail subscriptionExpireJobDetail;

    @Autowired
    private Trigger subscriptionExpireTrigger;

    @Autowired
    private SubscriptionExpireJob job;

    @Autowired
    private Scheduler scheduler;

    JobExecutionContext context;

    @BeforeEach
    void setUp() {
        context = mock(JobExecutionContext.class);

        // Context Mock 설정
        when(context.getMergedJobDataMap()).thenReturn(subscriptionExpireJobDetail.getJobDataMap());
        when(context.getJobDetail()).thenReturn(subscriptionExpireJobDetail);
        when(context.getTrigger()).thenReturn(subscriptionExpireTrigger);
        when(context.getScheduler()).thenReturn(scheduler);
    }
    /**
     * 스케줄러가 실제로 Job을 실행할 때는 JobExecutionContext와 JobDataMap이 자동으로 세팅되지만,
     * 테스트 코드에서 직접 execute()를 호출할 때는 직접 컨텍스트와 데이터를 세팅해줘야 함
     */
    @Test
    @DisplayName("비즈니스 로직이 정상적으로 호출되는지 테스트")
    void t1() throws Exception {
        //when
        job.execute(context);

        //then
        Mockito.verify(subscriptionService).updateSubscriptionStatus(); // 한 번 호출되었는지
    }

    @Test
    @DisplayName("Job 실패 시 재시도 로그 확인")
    void testRetryLogging(CapturedOutput output) {
        // given: Job 실행 후 예외 발생 시뮬레이션
        JobExecutionException exception = new JobExecutionException("테스트 예외");

        // when: 리스너 실행
        JobListener.jobWasExecuted(context, exception);

        // then: 로그 검증
        assertThat(output.getOut())
                .contains("Job 재시도 시도: 1/3회")
                .contains("subscriptionExpireJob 재시도 중 예외 발생")
                .contains("재시도 트리거 생성: subscriptionExpireTrigger-RETRY-1");
    }
}