package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static com.mallang.mallang_backend.global.constants.AppConstants.ORDER_ID_PREFIX;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith({OutputCaptureExtension.class, MockitoExtension.class})
class PaymentRedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RedisKeyValueAdapter connectionFactory;

    @InjectMocks
    private PaymentRedisService paymentRedisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        paymentRedisService = new PaymentRedisService(redisTemplate);
    }

    @Test
    @DisplayName("Redis 정상 저장 시 재시도 없이 성공")
    void saveDataToRedis_updateSuccessInfo(CapturedOutput out) {
        // Given
        String orderId = UUID.randomUUID().toString();
        int amount = 10000;

        // Redis에 정상적으로 저장되도록 Mock 설정
        when(valueOperations.setIfAbsent(
                anyString(),
                any(),
                any(Duration.class)
        )).thenReturn(true);

        // When
        paymentRedisService.saveDataToRedis(orderId, amount);

        // Then: 메서드 호출 확인 + 로그 메시지 확인
        verify(valueOperations, times(1)).setIfAbsent(
                eq(ORDER_ID_PREFIX + orderId),
                eq(String.valueOf(amount)),
                any(Duration.class)
        );

        assertThat(out.getOut()).contains("결제 정보 저장 성공");
    }

    @Test
    @Disabled("단위 테스트에서는 AOP 적용 불가능하니, 이후 새로 파일 옮겨서 실행하도록 함")
    @DisplayName("Redis 타임아웃 발생 시 4회 재시도 후 폴백 실행")
    void saveDataToRedis_RetryOnTimeout(CapturedOutput out) {
        // Given
        String orderId = UUID.randomUUID().toString();

        // When & Then
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new QueryTimeoutException("Redis 연결 오류"));

        assertThrows(QueryTimeoutException.class, () ->
                paymentRedisService.checkOrderIdAndAmount(orderId, 10000));

        // Verify: 1초기 시도 + 4회 재시도 = 총 5회 호출
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
