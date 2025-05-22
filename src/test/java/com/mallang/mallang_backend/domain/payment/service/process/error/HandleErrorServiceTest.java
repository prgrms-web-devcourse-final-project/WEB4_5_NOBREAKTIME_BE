package com.mallang.mallang_backend.domain.payment.service.process.error;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.global.init.factory.EntityTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.init.factory.EntityTestFactory.createPlan;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional

class HandleErrorServiceTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private ApplicationEventPublisher publisher;

    @Autowired
    private EntityTestFactory factory;

    @MockitoBean
    private PlanRepository planRepository;

    Member member;
    Plan plan;
    @BeforeEach
    void setUp() {
        member = factory.saveMember();
        plan = createPlan(SubscriptionType.STANDARD, PlanPeriod.MONTHLY);


    }

    @Test
    void handleApiFallback() {
    }

    @Test
    void handleSaveFailedFallback() {
    }
}