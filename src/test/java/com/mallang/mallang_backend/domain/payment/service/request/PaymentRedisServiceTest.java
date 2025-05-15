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

import static com.mallang.mallang_backend.global.constants.AppConstants.ORDER_ID_PREFIX;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void saveDataToRedis_updateSuccessInfo() {
        // Given
        String orderId = "order_456";
        int amount = 10000;

        // When & Then
        assertThatCode(() -> paymentRedisService.saveDataToRedis(
                orderId,
                amount
        ))
                .doesNotThrowAnyException();

        // Verify
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
        assertThatThrownBy(() -> paymentRedisService.saveDataToRedis(
                ORDER_ID_PREFIX + "order_456", 10000))
                .isInstanceOf(ServiceException.class);

        // Verify: 1초기 시도 + 4회 재시도 = 총 5회 호출
        verify(valueOperations, times(5)).set(
                anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Redis 에 저장 실패하였다면 DB에도 저장되지 않는다")
    void Verify_DB_Not_Updated_When_Redis_Save_Fails() {
        // given: Redis 모킹 설정
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        doThrow(new QueryTimeoutException("Connection failed"))
                .when(valueOps).set(anyString(), anyString(), any(Duration.class));

        // when
        assertThrows(ServiceException.class, () ->
                paymentRedisService.saveDataToRedis("order456", 10000));

        // then: DB에 주문 정보 미존재 검증
        assertFalse(paymentRepository.existsByOrderId("order456"));
    }
}
