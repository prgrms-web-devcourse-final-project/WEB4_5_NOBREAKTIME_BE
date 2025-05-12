package com.mallang.mallang_backend.global.config;

import java.time.Duration;
import java.util.Set;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
		// 1) JSON Serializer 준비
		GenericJackson2JsonRedisSerializer jsonSerializer =
			new GenericJackson2JsonRedisSerializer();

		// 2) 캐시 기본 설정: TTL 10분, null 금지, JSON 직렬화
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.disableCachingNullValues()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair
					.fromSerializer(new StringRedisSerializer())
			)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair
					.fromSerializer(jsonSerializer)
			);

		// 3) 미리 등록할 캐시 이름
		Set<String> cacheNames = Set.of("videoListCache" /*, otherCacheNames */);

		return RedisCacheManager.builder(factory)
			.cacheDefaults(config)
			.initialCacheNames(cacheNames)
			.build();
	}
}
