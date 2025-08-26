package com.und.server.notification.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.ScenarioNotificationListResponse;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.util.NotificationCacheKeyGenerator;
import com.und.server.notification.util.NotificationCacheSerializer;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.service.ScenarioNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCacheService {

	private static final int CACHE_TTL_DAYS = 30;
	private final RedisTemplate<String, Object> redisTemplate;
	private final NotificationCacheKeyGenerator keyGenerator;
	private final NotificationCacheSerializer serializer;
	private final NotificationConditionSelector notificationConditionSelector;
	private final ScenarioNotificationService scenarioNotificationService;


	public ScenarioNotificationListResponse getNotificationCache(Long memberId) {
		String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
		String etagKey = keyGenerator.generateEtagKey(memberId);

		String etag = (String) redisTemplate.opsForValue().get(etagKey);
		if (etag == null) {
			List<ScenarioNotificationResponse> scenarioNotifications =
				scenarioNotificationService.getScenarioNotifications(memberId);

			saveToCache(memberId, scenarioNotifications);
			String newEtag = updateEtag(memberId);

			return ScenarioNotificationListResponse.from(newEtag, scenarioNotifications);
		}

		Map<Object, Object> cacheData = redisTemplate.opsForHash().entries(cacheKey);
		if (cacheData.isEmpty()) {
			return new ScenarioNotificationListResponse(etag, new ArrayList<>());
		}

		List<ScenarioNotificationResponse> scenarios = new ArrayList<>();
		for (Object value : cacheData.values()) {
			NotificationCacheData cacheDto = serializer.deserialize((String) value);
			scenarios.add(convertToResponse(cacheDto));
		}

		return new ScenarioNotificationListResponse(etag, scenarios);
	}


	public void updateCache(Long memberId, Scenario scenario) {
		try {
			NotificationCacheData cacheData = createCacheData(scenario);

			String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
			String fieldKey = scenario.getId().toString();
			String jsonValue = serializer.serialize(cacheData);

			redisTemplate.opsForHash().put(cacheKey, fieldKey, jsonValue);
			redisTemplate.expire(cacheKey, CACHE_TTL_DAYS, TimeUnit.DAYS);

			updateEtag(memberId);

		} catch (Exception e) {
			log.error("Failed to update notification cache for memberId={}, scenarioId={}", memberId, scenario.getId(),
				e);
			throw new RuntimeException("Failed to update cache", e);
		}
	}


	public void deleteCache(Long memberId, Long scenarioId) {
		try {
			String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
			String fieldKey = scenarioId.toString();

			redisTemplate.opsForHash().delete(cacheKey, fieldKey);

			updateEtag(memberId);
		} catch (Exception e) {
			log.error("Failed to delete notification cache for memberId={}, scenarioId={}", memberId, scenarioId, e);
			throw new RuntimeException("Failed to delete from cache", e);
		}
	}


	public void deleteMemberAllCache(Long memberId) {
		try {
			String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
			String etagKey = keyGenerator.generateEtagKey(memberId);

			redisTemplate.delete(cacheKey);
			redisTemplate.delete(etagKey);

		} catch (Exception e) {
			log.error("Failed to delete notification cache for memberId={}", memberId, e);
			throw new RuntimeException("Failed to delete cache", e);
		}
	}


	private void saveToCache(Long memberId, List<ScenarioNotificationResponse> scenarioNotifications) {
		if (scenarioNotifications == null || scenarioNotifications.isEmpty()) {
			return;
		}
		String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
		Map<String, String> values = new HashMap<>();

		for (ScenarioNotificationResponse scenario : scenarioNotifications) {
			NotificationCacheData cacheData = NotificationCacheData.from(
				scenario,
				serializer.serializeCondition(scenario.notificationCondition())
			);

			String fieldKey = scenario.scenarioId().toString();
			String jsonValue = serializer.serialize(cacheData);
			values.put(fieldKey, jsonValue);
		}

		redisTemplate.opsForHash().putAll(cacheKey, values);
		redisTemplate.expire(cacheKey, CACHE_TTL_DAYS, TimeUnit.DAYS);
	}

	private String updateEtag(Long memberId) {
		String etagKey = keyGenerator.generateEtagKey(memberId);
		String etag = String.valueOf(System.currentTimeMillis());

		redisTemplate.opsForValue().set(etagKey, etag);
		redisTemplate.expire(etagKey, CACHE_TTL_DAYS, TimeUnit.DAYS);

		return etag;
	}

	private NotificationCacheData createCacheData(Scenario scenario) {
		NotificationConditionResponse condition =
			notificationConditionSelector.findNotificationCondition(scenario.getNotification());

		return NotificationCacheData.from(scenario, serializer.serializeCondition(condition));
	}

	private ScenarioNotificationResponse convertToResponse(NotificationCacheData notificationCacheData) {
		NotificationConditionResponse condition = serializer.parseCondition(notificationCacheData);

		return ScenarioNotificationResponse.from(notificationCacheData, condition);
	}

}
