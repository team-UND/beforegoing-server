package com.und.server.common.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories(
	enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP,
	keyspaceNotificationsConfigParameter = ""
)
@EnableCaching
public class RedisConfig {

	@Bean
	@Primary
	public CacheManager oidcCacheManager(final RedisConnectionFactory redisConnectionFactory) {
		final RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
			)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
			)
			.entryTtl(Duration.ofDays(3L));

		return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build();
	}

	@Bean
	public CacheManager missionCacheManager(final RedisConnectionFactory redisConnectionFactory) {
		final RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
			)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
			)
			.entryTtl(Duration.ofMinutes(30L));

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(config)
			.build();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());

		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

}
