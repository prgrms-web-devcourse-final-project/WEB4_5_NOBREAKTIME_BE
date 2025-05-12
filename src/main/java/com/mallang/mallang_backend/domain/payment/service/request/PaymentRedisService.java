package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.mallang.mallang_backend.global.constants.AppConstants.IDEM_KEY_PREFIX;
import static com.mallang.mallang_backend.global.constants.AppConstants.ORDER_ID_PREFIX;
import static com.mallang.mallang_backend.global.exception.ErrorCode.CONNECTION_FAIL;
import static com.mallang.mallang_backend.global.exception.ErrorCode.ORDER_ID_CONFLICT;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 주문 ID와 금액 / 멱등성 보장 토큰을 Redis 에 저장합니다.
     *
     * - redis 저장이 결제의 전제 조건
     *
     * @param orderId 주문 ID
     * @param amount  결제 금액
     */
    // TODO 동시 요청 시 값을 덮어 쓸 가능성이 있음 LOCK 적용
    @Retry(name = "redisConnectionRetry", fallbackMethod = "fallbackRedisException")
    public void saveDataToRedis(String idempotencyKey,
                                String orderId,
                                int amount) {

        String redisKey = IDEM_KEY_PREFIX + idempotencyKey;
        if (redisTemplate.hasKey(redisKey)) {
            log.warn("동일 주문에 대한 중복 요청 전송 - Key: {}", redisKey);
            throw new ServiceException(ORDER_ID_CONFLICT);
        }

        String redisIdemKey = IDEM_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(
                redisIdemKey,
                "processed",
                Duration.ofHours(24));

        String orderKey = ORDER_ID_PREFIX + orderId;
        redisTemplate.opsForValue().set(
                orderKey,
                String.valueOf(amount),
                Duration.ofHours(24));
    }

    private void fallbackRedisException(String idempotencyKey,
                                        String orderId,
                                        int amount,
                                        Exception e) {

        if (e instanceof QueryTimeoutException) {
            log.error("결제 임시 정보 저장 오류: {}", e.getMessage(), e);
            throw new ServiceException(CONNECTION_FAIL, e);
        }
        throw new ServiceException(ORDER_ID_CONFLICT, e);
    }
}
