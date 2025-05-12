package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.payment.dto.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableRetry
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
})
class PaymentRequestServiceTest {

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private RedisKeyValueAdapter connectionFactory;

    @Autowired
    private PaymentRequestService paymentRequestService;

    @MockitoBean
    private PaymentRedisService paymentRedisService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        // RedisTemplate → ValueOperations 체이닝 명시적 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Transactional
    @Test
    void 결제_요청_성공_및_DB_저장_검증() {
        // given: 테스트용 회원과 플랜 데이터가 DB에 있어야 함
        Member member = memberRepository.save(
                Member.builder()
                        .platformId("tes1")
                        .profileImageUrl("")
                        .loginPlatform(LoginPlatform.KAKAO)
                        .nickname("테스트회원")
                        .email("test@example1.com")
                        .language(Language.ENGLISH)
                        .build());

        PaymentSimpleRequest simpleRequest = new PaymentSimpleRequest(
                SubscriptionType.STANDARD,
                PlanPeriod.SIX_MONTHS
        );

        // Redis 멱등성 저장은 정상 동작한다고 가정
        doNothing().when(paymentRedisService).saveDataToRedis(anyString(), anyString(), anyInt());

        // when
        PaymentRequest paymentRequest = paymentRequestService.createPaymentRequest(
                "testKey123", member.getId(), simpleRequest);

        // then
        assertThat(paymentRequest).isNotNull();
        assertThat(paymentRequest.getOrderId()).isNotNull();

        // 실제 DB에 결제 정보가 저장되었는지 검증
        boolean exists = paymentRepository.existsByOrderId(paymentRequest.getOrderId());
        assertThat(exists).isTrue();
    }


    /*@Test
    void 분산락_동시_요청_검증() {
        when(redisTemplate.execute(any(), any(), any(), any()))
                .thenReturn(true)  // 첫 번째 스레드 락 획득
                .thenReturn(false); // 두 번째 스레드 락 획득 실패

        // 동시 요청 시뮬레이션
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> service.process("lockKey1")),
                CompletableFuture.runAsync(() -> service.process("lockKey1"))
        ).join();

        verify(redisTemplate, times(2)).execute(any(), any(), any(), any());
    }*/
}