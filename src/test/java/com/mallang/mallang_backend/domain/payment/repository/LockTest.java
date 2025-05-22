package com.mallang.mallang_backend.domain.payment.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import com.mallang.mallang_backend.global.config.QueryDslConfig;
import com.mallang.mallang_backend.global.init.factory.EntityTestFactory;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Disabled
@Slf4j
@DataJpaTest
@Import({
        EntityTestFactory.class,
        QueryDslConfig.class
})
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/test_db?sessionVariables=innodb_lock_wait_timeout=3",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.username=root",
        "spring.datasource.password=password",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.datasource.hikari.connection-init-sql=SET SESSION innodb_lock_wait_timeout=3"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LockTest {

    @Autowired
    private EntityTestFactory entityTestFactory;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    Member mem1;
    Member mem2;
    Member mem3;

    Plan stan;
    Plan pre;

    Clock clock;

    Payment payment1;
    Payment payment2;
    Payment payment3;

    @BeforeEach
    void setUp() {
        //given
        transactionTemplate = new TransactionTemplate(transactionManager);

        mem1 = entityTestFactory.saveMember();
        mem2 = entityTestFactory.saveMember();
        mem3 = entityTestFactory.saveMember();

        stan = entityTestFactory.savePlan(SubscriptionType.STANDARD, PlanPeriod.MONTHLY);
        pre = entityTestFactory.savePlan(SubscriptionType.PREMIUM, PlanPeriod.YEAR);

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        clock = Clock.fixed(oneHourAgo, ZoneId.systemDefault());

        payment1 = entityTestFactory.saveSuccessPayment(mem1.getId(), stan, clock);
        payment2 = entityTestFactory.saveSuccessPayment(mem2.getId(), pre, clock);
        payment3 = entityTestFactory.saveSuccessPayment(mem3.getId(), pre, clock);

        mem1.updateSubscription(SubscriptionType.STANDARD);
        mem2.updateSubscription(SubscriptionType.PREMIUM);
        mem3.updateSubscription(SubscriptionType.PREMIUM);
    }

    @AfterEach
    void tearDown() {
        //then
        paymentRepository.deleteAll();
        mem1 = null;
        mem2 = null;
        mem3 = null;
        stan = null;
        pre = null;
    }

    @Autowired
    private DataSource dataSource;

    @Test
    @Disabled("MySQL 직접 테스트 필요")
    void checkDataSource() {
        HikariDataSource hikari = (HikariDataSource) dataSource;
        assertThat(hikari.getJdbcUrl()).isEqualTo("jdbc:mysql://localhost:3306/test_db?sessionVariables=innodb_lock_wait_timeout=3");
        log.info("HikariDataSource: {}", hikari.getJdbcUrl());
        assertThat(hikari.getDriverClassName()).isEqualTo("com.mysql.cj.jdbc.Driver");
        log.info("HikariDataSource: {}", hikari.getDriverClassName());

        Map<String, Object> result = jdbcTemplate.queryForMap(
                "SHOW SESSION VARIABLES LIKE 'innodb_lock_wait_timeout'"
        );
        String timeout = result.get("Value").toString();
        log.info("innodb_lock_wait_timeout: {}", timeout);
    }

    /**
     * PessimisticLockException: 하나의 트랜잭션이 락을 잡고 있는 동안 다른 트랜잭션이 동시에 같은 자원에 락을 시도할 때
     * 락 대기 시간이 설정값(3초)을 넘어서면 두 번째 트랜잭션에서 예외가 발생
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Disabled("MySQL 직접 테스트 필요")
    @DisplayName("MySQL에서 PESSIMISTIC_WRITE 락 테스트")
    void t2() throws Exception {
        //given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);


        // 스레드 1: 락 선점 후 5초 대기
        Future<?> future1 = executor.submit(() -> {
            transactionTemplate.execute(status -> {
                paymentRepository.findByOrderIdWithLock(payment1.getOrderId());
                log.info("스레드 1: 락 선점");
                latch.countDown();
                sleep(4000); // 4초 동안 락 유지
                return null;
            });
        });

        latch.await(); // 스레드 1이 락을 잡을 때까지 대기

        // 스레드 2: 동시에 락 시도 -> 여기서 예외 발생
        Future<?> future2 = executor.submit(() -> {
            transactionTemplate.execute(status -> {
                log.info("스레드 2: 락 시도 시작");
                paymentRepository.findByOrderIdWithLock(payment1.getOrderId()); // 여기서 락 타임아웃 예외 발생 가능
                return null;
            });
        });

        // Then: 예외 발생 검증
        try {
            assertThatThrownBy(() -> future2.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(PessimisticLockingFailureException.class);
        } finally {
            sleep(2000);
            executor.shutdownNow(); // 모든 스레드 즉시 종료
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
