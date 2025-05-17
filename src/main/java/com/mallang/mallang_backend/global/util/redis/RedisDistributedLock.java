package com.mallang.mallang_backend.global.util.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RedisDistributedLock {

	private final RedisTemplate<String, String> redisTemplate;

	public RedisDistributedLock(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * Lock 획득을 시도합니다. true 반환 시 획득 성공입니다.
	 * @param key Lock의 key가 되는 문자열(영상 분석의 경우, videoId)
	 * @param value 자신을 구분할 수 있는 식별자(UUID 등 사용)
	 * @param expireMillis Lock 만료 시간
	 * @return 성공 여부
	 */
	public boolean tryLock(String key, String value, long expireMillis) {
		return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.set(
			key.getBytes(StandardCharsets.UTF_8),
			value.getBytes(StandardCharsets.UTF_8),
			Expiration.milliseconds(expireMillis),
			RedisStringCommands.SetOption.SET_IF_ABSENT
		)));
	}

	/**
	 * Lock을 해제합니다. true 반환 시 해제 성공입니다.
	 * @param key Lock의 key가 되는 문자열
	 * @param value 자신을 구분할 수 있는 식별자(UUID 등 사용)
	 * @return 성공 여부
	 */
	public boolean unlock(String key, String value) {
		String luaScript =
			"if redis.call('get', KEYS[1]) == ARGV[1] then " +
				"   return redis.call('del', KEYS[1]) " +
				"else return 0 end";

		return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection -> {
			Object result = connection.eval(
				luaScript.getBytes(),
				ReturnType.BOOLEAN,
				1,
				key.getBytes(StandardCharsets.UTF_8),
				value.getBytes(StandardCharsets.UTF_8)
			);
			return (Boolean) result;
		}));
	}

	/**
	 * 락 해제 여부를 주기적으로 체크하다가, 락이 풀리면 true 반환.
	 * 최대 대기 시간은 10분입니다.
	 * @param lockKey Lock 의 key 이름 문자열
	 * @param maxWaitMillis Lock 확인을 재시도할 최대 시간
	 * @param sleepMillis Lock 확인 재시도 간격
	 * @return 락이 풀렸는지 여부
	 * @throws InterruptedException
	 */
	public boolean waitForUnlockThenFetch(String lockKey, long maxWaitMillis, long sleepMillis) {
		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime < maxWaitMillis) {
			boolean exists = isLocked(lockKey);
			log.debug("Lock 해제 확인 : 현재 Lock 존재 여부 {}, {}", exists, System.currentTimeMillis() - startTime);
			if (!exists) {
				// 락이 사라진 경우
				return true;
			}
			try {
				Thread.sleep(sleepMillis);
			} catch (InterruptedException e) {
				// 인터럽트 여부를 상위 로직이나 다른 코드가 알 수 있게
				Thread.currentThread().interrupt();
				return false;
			}
		}
		// 최대 대기 시간 초과
		return false;
	}

	/**
	 * Lock이 현재 존재하는지 확인합니다.
	 * @param key Lock의 key
	 * @return 존재하면 true, 없으면 false
	 */
	private boolean isLocked(String key) {
		return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection ->
			connection.exists(key.getBytes(StandardCharsets.UTF_8))
		));
	}
}
