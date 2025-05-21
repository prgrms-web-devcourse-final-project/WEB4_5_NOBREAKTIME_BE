package com.mallang.mallang_backend.domain.subscription.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.entity.SubscriptionStatus;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.mallang.mallang_backend.domain.factory.EntityTestFactory.createMember;
import static com.mallang.mallang_backend.domain.factory.EntityTestFactory.createSubscription;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
class SubscriptionQueryRepositoryTest {

    @Autowired
    private SubscriptionQueryRepository queryRepository;

    @Autowired
    private EntityManager em;


    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("ACTIVE 상태이고, expiredAt이 오늘 0시보다 이전인 구독의 ID만 조회하는지 검증")
    void findActiveSubWithMember() throws Exception {
        //given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Member member = createMember();
        em.persist(member);
        Member member2 = createMember();
        em.persist(member2);
        Member member3 = createMember();
        em.persist(member3);
        log.info("저장된 회원: {}, {}, {}", member.getId(), member2.getId(), member3.getId());
        em.flush();

        Plan plan = em.find(Plan.class, 7L);// PREMIUM, MONTHLY

        // ACTIVE, 만료일 내일(조건에 안 맞음)
        Subscription sub1 = createSubscription(member, plan, todayStart.plusDays(1));
        em.persist(sub1);
        // ACTIVE, 만료일 오늘(조건에 맞음)
        Subscription sub2 = createSubscription(member2, plan, todayStart.minusHours(1));
        em.persist(sub2);
        // EXPIRED, 만료일 오늘(조건에 안 맞음)
        Subscription sub3 = createSubscription(member2, plan, todayStart.plusHours(1));
        sub3.updateStatus(SubscriptionStatus.EXPIRED);
        em.persist(sub3);

        em.flush();
        em.clear();

        log.info("저장된 구독: {}, {}, {}", sub1.getId(), sub2.getId(), sub3.getId());

        //when
        List<Long> ids = queryRepository.findActiveSubWithMember();

        //then
        log.info("ids: {}", ids);
        assertThat(ids).containsExactly(sub2.getId());
        assertThat(ids).doesNotContain(sub1.getId());
    }

    @Test
    @DisplayName("전달받은 ID들의 구독 상태가 EXPIRED로 정상 변경되는지 검증")
    void bulkUpdateStatus() throws Exception {
        //given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Member member = createMember();
        em.persist(member);
        Member member2 = createMember();
        em.persist(member2);
        Member member3 = createMember();
        em.persist(member3);
        em.flush();

        Plan plan = em.find(Plan.class, 7L);// PREMIUM, MONTHLY

        // ACTIVE, 만료일 어제(조건에 맞음)
        Subscription sub1 = createSubscription(member, plan, todayStart.minusDays(1));
        em.persist(sub1);
        Subscription sub2 = createSubscription(member, plan, todayStart.minusDays(1));
        em.persist(sub2);
        Subscription sub3 = createSubscription(member, plan, todayStart.minusDays(1));
        em.persist(sub3);

        //when
        long count = queryRepository.bulkUpdateStatus(List.of(1L, 2L, 3L));

        //then
        assertThat(count).isEqualTo(3);
        assertThat(em.find(Subscription.class, 1L).getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(em.find(Subscription.class, 2L).getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(em.find(Subscription.class, 3L).getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }
}