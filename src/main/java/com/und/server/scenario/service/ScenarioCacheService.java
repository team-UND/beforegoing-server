package com.und.server.scenario.service;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.und.server.notification.constants.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioCacheService {

	private final RedisTemplate<String, Object> redisTemplate;

	public void evictAllScenarioCache() {
		evictCacheByPattern("scenarios::*", "all scenarios");
	}

	public void evictUserScenarioCache(final Long memberId) {
		evictCacheByPattern("scenarios::" + memberId + ":*",
			"member: " + memberId);
	}

	public void evictUserScenarioCache(final Long memberId, final NotificationType notificationType) {
		try {
			String key = "scenarios::" + memberId + ":" + notificationType;
			Boolean deleted = redisTemplate.delete(key);

		} catch (Exception e) {
			log.error("Failed to evict specific scenario cache for member: {}, notificationType: {}",
				memberId, notificationType, e);
		}
	}

	private void evictCacheByPattern(final String pattern, final String description) {
		try {
			Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
				Set<String> matchingKeys = new HashSet<>();
				ScanOptions options = ScanOptions.scanOptions()
					.match(pattern)
					.count(100)
					.build();

				try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
					while (cursor.hasNext()) {
						matchingKeys.add(new String(cursor.next(), StandardCharsets.UTF_8));
					}
				}
				return matchingKeys;
			});

			if (keys != null && !keys.isEmpty()) {
				redisTemplate.delete(keys);
				log.debug("Evicted {} scenario cache keys for {}", keys.size(), description);
			}
		} catch (Exception e) {
			log.error("Failed to evict scenario cache for {}", description, e);
		}
	}
}
