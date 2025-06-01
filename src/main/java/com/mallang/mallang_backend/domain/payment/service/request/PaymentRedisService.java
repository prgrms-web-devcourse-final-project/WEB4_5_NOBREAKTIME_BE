package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mallang.mallang_backend.global.constants.AppConstants.IDEM_KEY_PREFIX;
import static com.mallang.mallang_backend.global.constants.AppConstants.ORDER_ID_PREFIX;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * 주문 ID와 금액을 Redis 에 저장합니다.
     * <p>
     * - redis 저장이 결제의 전제 조건
     *
     * @param orderId 주문 ID
     * @param amount  결제 금액
     */
    @Retry(name = "redisSave", fallbackMethod = "fallbackMethod")
    public void saveDataToRedis(String orderId,
                                int amount
    ) {
        String orderKey = ORDER_ID_PREFIX + orderId;

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(orderKey, String.valueOf(amount), Duration.ofHours(24));

        if (!Boolean.TRUE.equals(success)) {
            log.error("이미 존재하는 주문 ID: {}", orderId);
            throw new ServiceException(PAYMENT_CONFLICT);
        }

        log.info("결제 정보 저장 성공: orderId: {}, amount: {}",
                orderId, amount);
    }

    /**
     * redis 에서 저장된 주문 ID와 결제 금액이 일치하는지 확인 (결제 승인 전)
     *
     * 일반적으로 동시성 문제가 발생할 것이라고 보여지진 않음
     * 이 값을 기반으로 결제 정보를 update 해야 하는 것 -> 여기서 문제가 발생할 수도 있을 것 같다
     */
    @Retry(name = "redisSave", fallbackMethod = "fallbackMethod")
    public void checkOrderIdAndAmount(String orderId,
                                      int amount
    ) {

        String redisKey = ORDER_ID_PREFIX + orderId;
        Object value = redisTemplate.opsForValue().get(redisKey);

        log.debug("결제 정보 검증 시작: orderId: {}, amount: {}, value: {}",
                orderId, amount, value);

        Integer savedAmount = Optional.ofNullable(value)
                .map(Object::toString)
                .map(Integer::valueOf)
                .orElseThrow(() -> new ServiceException(PAYMENT_NOT_FOUND));

        if (!savedAmount.equals(amount)) {
            throw new ServiceException(ORDER_AMOUNT_MISMATCH);
        }

        log.info("결제 정보 검증 성공: orderId: {}, amount: {}",
                orderId, amount);
    }

    /**
     * hasKey → set: Race Condition 위반 (둘 다 키 없음으로 확인할 것임)
     * setIfAbsent: 키가 없으면 저장, 이미 있으면 저장하지 않음 (원자적 실행)
     * → 여러 요청이 동시에 들어와도, 오직 하나의 요청만 true를 받고 나머지는 false를 받음
     */
    @Retry(name = "redisSave", fallbackMethod = "fallbackMethod")
    public void checkAndSaveIdempotencyKey(String idempotencyKey
    ) {
        String redisIdemKey = IDEM_KEY_PREFIX + idempotencyKey;

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisIdemKey, "processed", Duration.ofHours(24));

        if (!Boolean.TRUE.equals(success)) {
            log.error("동일 주문에 대한 중복 결제 전송 - Key: {}", redisIdemKey);
            throw new ServiceException(PAYMENT_CONFLICT);
        }

        log.info("멱등성 토큰 키 저장 성공: {}", idempotencyKey);
    }

    public void fallbackMethod(String orderId,
                                  int amount,
                                  Throwable t) {

        if (t instanceof QueryTimeoutException) {
            log.error(
                    "Redis 타임아웃 발생: {}",
                    Map.of(
                            "type", "REDIS_TIMEOUT",
                            "orderId", orderId,
                            "error", t.getMessage(),
                            "retryCount", retryCount.incrementAndGet()
                    ),
                    t
            );
        }
        log.error("Redis 연결 실패: orderId: {}, amount: {}", orderId, amount, t.getCause());
        throw new ServiceException(REDIS_CONNECTION_FAILED, t.getCause());
    }

    // 자동 결제 실패 시 해당 내용을 삭제하고 재시도 할 수 있도록 함
    public void deleteIdempotencyKey(String customerKey, String orderId) {
        String customerOrderIdKey = IDEM_KEY_PREFIX + orderId + "-" + customerKey;

        if (redisTemplate.hasKey(customerOrderIdKey)) {
            redisTemplate.delete(customerOrderIdKey);
        } else {
            log.warn("삭제할 키가 없습니다: {}", customerOrderIdKey);
        }
    }
}