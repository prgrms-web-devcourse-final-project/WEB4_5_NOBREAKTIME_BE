package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

import static com.mallang.mallang_backend.global.constants.AppConstants.IDEM_KEY_PREFIX;
import static com.mallang.mallang_backend.global.constants.AppConstants.ORDER_ID_PREFIX;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 주문 ID와 금액을 Redis 에 저장합니다.
     * <p>
     * - redis 저장이 결제의 전제 조건
     *
     * @param orderId 주문 ID
     * @param amount  결제 금액
     */
    // TODO 동시 요청 시 값을 덮어 쓸 가능성이 있음 LOCK 적용
    @Retry(name = "redisConnectionRetry", fallbackMethod = "fallbackMethod")
    public void saveDataToRedis(String orderId,
                                int amount
    ) {
        String orderKey = ORDER_ID_PREFIX + orderId;
        redisTemplate.opsForValue().set(
                orderKey,
                String.valueOf(amount),
                Duration.ofHours(24));
    }

    // redis 에서 저장된 주문 ID와 결제 금액이 일치하는지 확인
    @Retry(name = "redisConnectionRetry", fallbackMethod = "fallbackMethod")
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
            throw new ServiceException(PAYMENT_NOT_FOUND);
        }

        log.info("결제 정보 검증 성공: orderId: {}, amount: {}",
                orderId, amount);
    }

    public void checkAndSaveIdempotencyKey(String idempotencyKey
    ) {
        String redisIdemKey = IDEM_KEY_PREFIX + idempotencyKey;

        if (redisTemplate.hasKey(redisIdemKey)) {
            log.warn("동일 주문에 대한 중복 결제 전송 - Key: {}", redisIdemKey);
            throw new ServiceException(PAYMENT_CONFLICT);
        }

        redisTemplate.opsForValue().set(
                redisIdemKey,
                "processed",
                Duration.ofHours(24));
        log.info("멱등성 토큰 키 저장 성공: {}", idempotencyKey);
    }

    private void fallbackMethod(String orderId,
                                  int amount,
                                  Exception e
    ) {
        if (e instanceof QueryTimeoutException) {
            log.error("결제 요청 중 오류 발생: {}", e.getMessage(), e);
            throw new ServiceException(CONNECTION_FAIL, e);
        }
        throw new ServiceException(PAYMENT_CONFLICT, e);
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