package com.mallang.mallang_backend.domain.payment.quartz.job;

import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static com.mallang.mallang_backend.global.exception.ErrorCode.SUBSCRIPTION_STATUS_UPDATE_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class SubscriptionExpireJobTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    @InjectMocks
    private SubscriptionExpireJob job;

    @Test
    @DisplayName("예외 발생 시 재시도 로직이 정상적으로 실행되는지 테스트")
    void t1(CapturedOutput out) throws Exception {
        //given
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("currentRetry", 1);
        dataMap.put("maxRetry", 3);

        when(context.getMergedJobDataMap()).thenReturn(dataMap);
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("subscriptionExpireJob", "DEFAULT"));

        //when: subscriptionService.updateSubscriptionStatus() 호출 시 예외 발생하도록 설정
        doThrow(new ServiceException(SUBSCRIPTION_STATUS_UPDATE_FAILED)).when(subscriptionService).updateSubscriptionStatus();

        assertThrows(JobExecutionException.class,
                () -> job.execute(context)
        );

        //then
        assertThat(out.getOut()).contains("구독 변경 Job 실행 중 예외 발생 - 재시도 횟수: 1/3");
    }
}