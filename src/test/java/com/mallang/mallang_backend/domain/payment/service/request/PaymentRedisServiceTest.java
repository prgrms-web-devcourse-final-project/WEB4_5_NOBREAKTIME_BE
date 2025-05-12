package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mallang.mallang_backend.global.constants.AppConstants.IDEM_KEY_PREFIX;
import static com.mallang.mallang_backend.global.constants.AppConstants.ORDER_ID_PREFIX;
import static com.mallang.mallang_backend.global.exception.ErrorCode.ORDER_ID_CONFLICT;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentRedisServiceTest {

    @Autowired
    private PaymentRedisService paymentRedisService;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private ValueOperations<String, Object> valueOperations;

    @MockitoBean
    private RedisKeyValueAdapter connectionFactory;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Redis 정상 저장 시 재시도 없이 성공")
    void saveDataToRedis_Success() {
        // Given
        String idempotencyKey = "idem_123";
        String orderId = "order_456";
        int amount = 10000;

        // When & Then
        assertThatCode(() -> paymentRedisService.saveDataToRedis(
                idempotencyKey,
                orderId,
                amount
                ))
                .doesNotThrowAnyException();

        // Verify
        verify(valueOperations, times(1)).set(eq(IDEM_KEY_PREFIX + "idem_123"),
                eq("processed"), any(Duration.class));
        verify(valueOperations, times(1)).set(eq(ORDER_ID_PREFIX + "order_456"),
                eq("10000"), any(Duration.class));
    }

    @Test
    @DisplayName("Redis 타임아웃 발생 시 4회 재시도 후 폴백 실행")
    void saveDataToRedis_RetryOnTimeout() {
        // Given
        doThrow(new QueryTimeoutException("Connection timeout"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When & Then
        assertThatThrownBy(() -> paymentRedisService.saveDataToRedis(IDEM_KEY_PREFIX + "idem_123",
                ORDER_ID_PREFIX + "order_456", 10000))
                .isInstanceOf(ServiceException.class);

        // Verify: 1초기 시도 + 4회 재시도 = 총 5회 호출
        verify(valueOperations, times(5)).set(
                anyString(), anyString(), any(Duration.class));
    }

    @Test
    void Redis_저장_실패시_DB_미반영_검증() {
        // given: Redis 모킹 설정
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        doThrow(new QueryTimeoutException("Connection failed"))
                .when(valueOps).set(anyString(), anyString(), any(Duration.class));

        // when
        assertThrows(ServiceException.class, () ->
                paymentRedisService.saveDataToRedis("token123", "order456", 10000));

        // then: DB에 주문 정보 미존재 검증
        assertFalse(paymentRepository.existsByOrderId("order456"));
    }

    @Test
    void 멱등성_토큰_중복_요청_방지_검증() {
        // Redis 모킹 설정
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // 키 존재 상태 추적용 AtomicBoolean
        AtomicBoolean isKeyExist = new AtomicBoolean(false);

        // hasKey() 동작 제어
        when(redisTemplate.hasKey(IDEM_KEY_PREFIX + "dupToken"))
                .thenAnswer(inv -> isKeyExist.get());

        // set() 호출시 키 존재 상태 변경
        doAnswer(inv -> {
            isKeyExist.set(true);
            return null;
        }).when(valueOps).set(eq(IDEM_KEY_PREFIX + "dupToken"), any(), any(Duration.class));

        // 첫 번째 요청 성공
        paymentRedisService.saveDataToRedis("dupToken", "order789", 20000);

        // 두 번째 요청 시 예외 발생 검증
        ServiceException exception = assertThrows(ServiceException.class,
                () -> paymentRedisService.saveDataToRedis("dupToken", "order789", 20000)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ORDER_ID_CONFLICT);
        verify(valueOps, times(2)).set(anyString(), any(), any(Duration.class));
    }
}
