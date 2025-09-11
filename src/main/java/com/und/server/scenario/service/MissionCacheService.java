package com.und.server.scenario.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionCacheService {

	private final RedisTemplate<String, Object> redisTemplate;

	public void evictAllMissionCache() {
		evictCacheByPattern("missions::*", "all missions");
	}

	public void evictUserMissionCache(final Long memberId) {
		evictCacheByPattern("missions::" + memberId + ":*",
			"member: " + memberId);
	}

	public void evictUserMissionCache(final Long memberId, final Long scenarioId) {
		evictCacheByPattern("missions::" + memberId + ":" + scenarioId + ":*",
			"member: " + memberId + ", scenario: " + scenarioId);
	}

	public void evictUserMissionCache(final Long memberId, final Long scenarioId, final LocalDate date) {
		try {
			String key = "missions::" + memberId + ":" + scenarioId + ":" + date;
			Boolean deleted = redisTemplate.delete(key);

		} catch (Exception e) {
			log.error("Failed to evict specific mission cache for member: {}, scenario: {}, date: {}",
				memberId, scenarioId, date, e);
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
			}
		} catch (Exception e) {
			log.error("Failed to evict mission cache for {}", description, e);
		}
	}

}
