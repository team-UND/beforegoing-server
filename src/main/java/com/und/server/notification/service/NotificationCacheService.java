package com.und.server.notification.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

		// ETag 조회
		String etag = (String) redisTemplate.opsForValue().get(etagKey);
		if (etag == null) {
			List<ScenarioNotificationResponse> scenarioNotifications =
				scenarioNotificationService.getScenarioNotifications(memberId);

			String newEtag = updateEtagAndGet(memberId);
			saveToCache(memberId, scenarioNotifications);

			return ScenarioNotificationListResponse.from(newEtag, scenarioNotifications);
		}

		// 캐시 데이터 조회
		Map<Object, Object> cacheData = redisTemplate.opsForHash().entries(cacheKey); //todo 왜 Object인지
		if (cacheData.isEmpty()) {
			return new ScenarioNotificationListResponse(etag, new ArrayList<>());
		}

		// 캐시 데이터를 응답 형태로 변환
		List<ScenarioNotificationResponse> scenarios = new ArrayList<>();
		for (Object value : cacheData.values()) {
			NotificationCacheData cacheDto = serializer.deserialize((String) value);
			scenarios.add(convertToResponse(cacheDto));
		}

		return new ScenarioNotificationListResponse(etag, scenarios);
	}

	/**
	 * 캐시 업데이트 (시나리오 단위)
	 */
	public void updateCache(Long memberId, Scenario scenario) {
		try {
			// NotificationCacheData 생성
			NotificationCacheData cacheData = createCacheData(scenario);

			// Redis에 저장
			String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
			String fieldKey = scenario.getId().toString();
			String jsonValue = serializer.serialize(cacheData);

			redisTemplate.opsForHash().put(cacheKey, fieldKey, jsonValue);

			// TTL 갱신
			redisTemplate.expire(cacheKey, CACHE_TTL_DAYS, TimeUnit.DAYS);

			// ETag 업데이트
			updateEtag(memberId);

			log.info("Updated notification cache for memberId={}, scenarioId={}", memberId, scenario.getId());
		} catch (Exception e) {
			log.error("Failed to update notification cache for memberId={}, scenarioId={}", memberId, scenario.getId(),
				e);
			throw new RuntimeException("Failed to update cache", e);
		}
	}

	/**
	 * 캐시에서 특정 시나리오 삭제
	 */
	public void deleteFromCache(Long memberId, Long scenarioId) {
		try {
			String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
			String fieldKey = scenarioId.toString();

			redisTemplate.opsForHash().delete(cacheKey, fieldKey);

			// ETag 업데이트
			updateEtag(memberId);

			log.info("Deleted notification cache for memberId={}, scenarioId={}", memberId, scenarioId);
		} catch (Exception e) {
			log.error("Failed to delete notification cache for memberId={}, scenarioId={}", memberId, scenarioId, e);
			throw new RuntimeException("Failed to delete from cache", e);
		}
	}

	/**
	 * 사용자 캐시 전체 삭제 (동기화 실패 대비)
	 */
	public void deleteCache(Long memberId) {
		try {
			String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);
			String etagKey = keyGenerator.generateEtagKey(memberId);

			redisTemplate.delete(cacheKey);
			redisTemplate.delete(etagKey);

			log.info("Deleted all notification cache for memberId={}", memberId);
		} catch (Exception e) {
			log.error("Failed to delete notification cache for memberId={}", memberId, e);
			throw new RuntimeException("Failed to delete cache", e);
		}
	}


	private void saveToCache(Long memberId, List<ScenarioNotificationResponse> scenarioNotifications) {
		String cacheKey = keyGenerator.generateNotificationCacheKey(memberId);

		for (ScenarioNotificationResponse scenario : scenarioNotifications) {
			NotificationCacheData cacheData = NotificationCacheData.from(
				scenario,
				serializer.serializeCondition(scenario.notificationCondition())
			);

			String fieldKey = scenario.scenarioId().toString();
			String jsonValue = serializer.serialize(cacheData);
			redisTemplate.opsForHash().put(cacheKey, fieldKey, jsonValue);
		}
		redisTemplate.expire(cacheKey, CACHE_TTL_DAYS, TimeUnit.DAYS);
	}

	private String updateEtagAndGet(Long memberId) {
		String etagKey = keyGenerator.generateEtagKey(memberId);
		String etag = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		redisTemplate.opsForValue().set(etagKey, etag);
		redisTemplate.expire(etagKey, CACHE_TTL_DAYS, TimeUnit.DAYS);

		return etag;
	}

	/**
	 * ETag 업데이트
	 */
	private void updateEtag(Long memberId) {
		updateEtagAndGet(memberId);
	}

	/**
	 * 시나리오를 NotificationCacheData로 변환
	 */
	private NotificationCacheData createCacheData(Scenario scenario) {
		NotificationConditionResponse condition =
			notificationConditionSelector.findNotificationCondition(scenario.getNotification());

		return NotificationCacheData.from(scenario, serializer.serializeCondition(condition));
	}

	/**
	 * NotificationCacheData를 ScenarioNotificationResponse로 변환
	 */
	private ScenarioNotificationResponse convertToResponse(NotificationCacheData notificationCacheData) {
		NotificationConditionResponse condition = serializer.parseCondition(notificationCacheData);

		return ScenarioNotificationResponse.from(notificationCacheData, condition);
	}

}
