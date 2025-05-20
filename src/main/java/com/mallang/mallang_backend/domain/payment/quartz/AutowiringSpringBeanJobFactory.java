package com.mallang.mallang_backend.domain.payment.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * 쿼츠 스케줄러는 Job을 인스턴스를 직접 생성, @Autowired 으로 빈을 주입 받을 수가 없음
 * = SpringBeanJobFactory를 확장하여 Job 생성 시점에 Spring의 의존성 주입을 강제로 수행
 */
@Slf4j
public class AutowiringSpringBeanJobFactory extends
        SpringBeanJobFactory implements ApplicationContextAware {

    private AutowireCapableBeanFactory beanFactory;

    // Spring 애플리케이션 컨텍스트 주입
    @Override
    public void setApplicationContext(ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

    // Job 인스턴스 생성 시 의존성 주입 수행
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        // 1. Job 클래스 타입 가져오기
        Class<?> jobClass = bundle.getJobDetail().getJobClass();

        // 2. Spring이 관리하는 빈으로 생성 (의존성 주입 완료)
        Object job = beanFactory.createBean(jobClass);

        // 3. 로깅
        log.info("Job 인스턴스 생성 및 의존성 주입 완료: {}", job.getClass().getSimpleName());
        return job;
    }
}
