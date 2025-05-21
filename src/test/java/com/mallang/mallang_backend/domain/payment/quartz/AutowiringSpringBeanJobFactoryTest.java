package com.mallang.mallang_backend.domain.payment.quartz;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.*;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import static com.mallang.mallang_backend.domain.payment.quartz.AutowiringSpringBeanJobFactoryTest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@Import({MyJob.class, SomeService.class})
class AutowiringSpringBeanJobFactoryTest {

    @Autowired
    private ApplicationContext context;


    @ToString
    static class MyJob implements Job {
        @Autowired
        private SomeService someService;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            log.info("MyJob 실행");
            someService.doSomething();
            log.info("MyJob 실행 완료");
        }
    }

    @ToString
    @Service
    static class SomeService {
        public void doSomething() {
            log.info("SomeService 실행");
        }
    }

    @Test
    public void testJobInstanceAutowired(CapturedOutput output) throws Exception {
        AutowiringSpringBeanJobFactory factory = new AutowiringSpringBeanJobFactory();
        factory.setApplicationContext(context);

        JobDetail jobDetail = JobBuilder.newJob(MyJob.class)
                .withIdentity("testJob", "group1")
                .build();

        // 2. OperableTrigger Mock 생성
        OperableTrigger trigger = mock(OperableTrigger.class);
        when(trigger.getKey()).thenReturn(TriggerKey.triggerKey("testTrigger", "group1"));
        when(trigger.getJobDataMap()).thenReturn(jobDetail.getJobDataMap()); // JobDataMap이 null이 되지 않도록

        // 3. TriggerFiredBundle 생성
        TriggerFiredBundle bundle = new TriggerFiredBundle(
                jobDetail,
                trigger,
                null, // calendar
                false, // isRecovering
                null, // fireTime
                null, // scheduledFireTime
                null, // prevFireTime
                null  // nextFireTime
        );

        MyJob job = (MyJob) factory.createJobInstance(bundle);

        assertNotNull(job, "Job 인스턴스가 생성되어야 합니다.");
        assertThat(output.getOut())
                .contains("MyJob(someService=AutowiringSpringBeanJobFactoryTest.SomeService()");
    }
}