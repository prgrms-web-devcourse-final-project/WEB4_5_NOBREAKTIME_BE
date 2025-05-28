package com.mallang.mallang_backend.global.config;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
		// JSON 직렬화기 준비
		GenericJackson2JsonRedisSerializer jsonSerializer =
			new GenericJackson2JsonRedisSerializer();

		// 캐시 기본 설정: TTL 24시간, null 금지, JSON 직렬화
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofHours(24))
			.disableCachingNullValues()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair
					.fromSerializer(new StringRedisSerializer())
			)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair
					.fromSerializer(jsonSerializer)
			);

		// 단어퀴즈 및 표현퀴즈 캐시 전용 설정: TTL 1시간
		RedisCacheConfiguration quizCacheConfig = defaultConfig
			.entryTtl(Duration.ofHours(1));

		// 캐시 이름 목록
		Set<String> cacheNames = Set.of(
			"videoListCache",
			"wordQuizCache",
			"expressionQuizCache"
		);

		// 캐시별 전용 설정 매핑
		Map<String, RedisCacheConfiguration> perCacheConfigs = Map.of(
			"wordQuizCache", quizCacheConfig,
			"expressionQuizCache", quizCacheConfig
		);

		// RedisCacheManager 생성
		return RedisCacheManager.builder(factory)
			.cacheDefaults(defaultConfig)
			.withInitialCacheConfigurations(perCacheConfigs)
			.initialCacheNames(cacheNames)
			.build();
	}
}
