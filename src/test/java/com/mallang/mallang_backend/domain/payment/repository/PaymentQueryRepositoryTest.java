package com.mallang.mallang_backend.domain.payment.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.global.config.QueryDslConfig;
import com.mallang.mallang_backend.global.init.factory.EntityTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static com.mallang.mallang_backend.domain.payment.service.process.complete.CompletePayServiceImpl.*;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
@Import({
        QueryDslConfig.class,
        OutputCaptureExtension.class
})
class PaymentQueryRepositoryTest {

    @Autowired
    private EntityTestFactory entityTestFactory;

    @Autowired
    private PaymentQueryRepository queryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlanRepository repository;

    Member mem1;
    Member mem2;
    Member mem3;

    Clock clock;

    Plan stan;
    Plan pre;

    Payment payment1;
    Payment payment2;
    Payment payment3;

    MemberGrantedInfo info1;
    MemberGrantedInfo info2;
    MemberGrantedInfo info3;

    @BeforeEach
    @Transactional
    void setUp() {
        //given
        mem1 = entityTestFactory.saveMember();
        mem2 = entityTestFactory.saveMember();
        mem3 = entityTestFactory.saveMember();

        stan = repository.findById(4L).get();
        pre = repository.findById(7L).get();

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        clock = Clock.fixed(oneHourAgo, ZoneId.systemDefault());

        payment1 = entityTestFactory.saveSuccessPayment(mem1.getId(), stan, clock);
        payment2 = entityTestFactory.saveSuccessPayment(mem2.getId(), pre, clock);
        payment3 = entityTestFactory.saveSuccessPayment(mem3.getId(), pre, clock);

        mem1.updateSubscription(SubscriptionType.STANDARD);
        mem2.updateSubscription(SubscriptionType.PREMIUM);
        mem3.updateSubscription(SubscriptionType.PREMIUM);

        info1 = queryRepository.findMemberGrantedInfoWithRole(payment1.getOrderId());
        info2 = queryRepository.findMemberGrantedInfoWithRole(payment2.getOrderId());
        info3 = queryRepository.findMemberGrantedInfoWithRole(payment3.getOrderId());
    }

    @Test
    @Transactional
    @DisplayName("orderId 기반으로 멤버의 권한 정보 가져오기")
    void t1() throws Exception {
        // when & then
        assertThat(info1.memberId()).isEqualTo(mem1.getId());
        assertThat(info1.type()).isEqualTo(SubscriptionType.STANDARD);

        assertThat(info2.memberId()).isEqualTo(mem2.getId());
        assertThat(info2.type()).isEqualTo(SubscriptionType.PREMIUM);

        assertThat(info3.memberId()).isEqualTo(mem3.getId());
        assertThat(info3.type()).isEqualTo(SubscriptionType.PREMIUM);
    }
}
