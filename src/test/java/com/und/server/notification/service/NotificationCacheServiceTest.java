package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.dto.response.ScenarioNotificationListResponse;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationCacheException;
import com.und.server.notification.util.NotificationCacheKeyGenerator;
import com.und.server.notification.util.NotificationCacheSerializer;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.service.ScenarioNotificationService;


@ExtendWith(MockitoExtension.class)
class NotificationCacheServiceTest {

	@InjectMocks
	private NotificationCacheService notificationCacheService;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@Mock
	private NotificationCacheKeyGenerator keyGenerator;

	@Mock
	private NotificationCacheSerializer serializer;

	@Mock
	private NotificationConditionSelector notificationConditionSelector;

	@Mock
	private ScenarioNotificationService scenarioNotificationService;

	private final Long memberId = 1L;
	private final Long scenarioId = 10L;
	private final String cacheKey = "notif:1";
	private final String etagKey = "notif:etag:1";


	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
	}


	@Test
	void Given_CacheHit_When_GetScenariosNotificationCache_Then_ReturnCachedData() {
		// given
		String etag = "1234567890";
		Map<Object, Object> cacheData = new HashMap<>();
		cacheData.put("10", "serialized_cache_data");

		NotificationCacheData notificationCacheData = NotificationCacheData.builder()
			.scenarioId(scenarioId)
			.scenarioName("아침 루틴")
			.scenarioMemo("아침에 할 일들")
			.build();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn(etag);
		given(hashOperations.entries(cacheKey)).willReturn(cacheData);
		given(serializer.deserialize("serialized_cache_data")).willReturn(notificationCacheData);
		given(serializer.parseCondition(notificationCacheData)).willReturn(null);

		// when
		ScenarioNotificationListResponse result = notificationCacheService.getScenariosNotificationCache(memberId);

		// then
		assertThat(result.etag()).isEqualTo(etag);
		assertThat(result.scenarios()).hasSize(1);
		assertThat(result.scenarios().get(0).scenarioId()).isEqualTo(scenarioId);
		verify(scenarioNotificationService, never()).getScenarioNotifications(any());
	}


	@Test
	void Given_CacheMiss_When_GetScenariosNotificationCache_Then_ReturnFromDatabase() {
		// given
		ScenarioNotificationResponse dbResponse = ScenarioNotificationResponse.builder()
			.scenarioId(scenarioId)
			.scenarioName("아침 루틴")
			.memo("아침에 할 일들")
			.notificationCondition(null)
			.build();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn(null);
		given(scenarioNotificationService.getScenarioNotifications(memberId)).willReturn(List.of(dbResponse));

		// when
		ScenarioNotificationListResponse result = notificationCacheService.getScenariosNotificationCache(memberId);

		// then
		assertThat(result.scenarios()).hasSize(1);
		assertThat(result.scenarios().get(0).scenarioId()).isEqualTo(scenarioId);
		verify(scenarioNotificationService).getScenarioNotifications(memberId);
	}


	@Test
	void Given_ValidScenario_When_GetSingleScenarioNotificationCache_Then_ReturnNotification() {
		// given
		String cachedValue = "serialized_cache_data";
		NotificationCacheData notificationCacheData = NotificationCacheData.builder()
			.scenarioId(scenarioId)
			.scenarioName("아침 루틴")
			.scenarioMemo("아침에 할 일들")
			.build();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");
		given(hashOperations.get(cacheKey, scenarioId.toString())).willReturn(cachedValue);
		given(serializer.deserialize(cachedValue)).willReturn(notificationCacheData);
		given(serializer.parseCondition(notificationCacheData)).willReturn(null);

		// when
		ScenarioNotificationResponse result =
			notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.scenarioId()).isEqualTo(scenarioId);
		assertThat(result.scenarioName()).isEqualTo("아침 루틴");
	}


	@Test
	void Given_NonExistentScenario_When_GetSingleScenarioNotificationCache_Then_ReturnNull() {
		// given
		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");
		given(hashOperations.get(cacheKey, scenarioId.toString())).willReturn(null);

		// when
		ScenarioNotificationResponse result =
			notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId);

		// then
		assertThat(result).isNull();
	}


	@Test
	void Given_ValidScenario_When_UpdateCache_Then_UpdateSuccessfully() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("업데이트된 루틴")
			.memo("업데이트된 메모")
			.notification(notification)
			.build();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");
		given(notificationConditionSelector.findNotificationCondition(notification)).willReturn(null);
		given(serializer.serializeCondition(null)).willReturn("serialized_condition");
		given(serializer.serialize(any())).willReturn("serialized_cache_data");

		// when
		notificationCacheService.updateCache(memberId, scenario);

		// then
		verify(hashOperations).put(eq(cacheKey), eq(scenarioId.toString()), eq("serialized_cache_data"));
		verify(redisTemplate).expire(eq(cacheKey), anyLong(), any(TimeUnit.class));
		verify(valueOperations).set(eq(etagKey), anyString());
		verify(redisTemplate).expire(eq(etagKey), anyLong(), any(TimeUnit.class));
	}


	@Test
	void Given_ValidScenario_When_DeleteCache_Then_DeleteSuccessfully() {
		// given
		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");

		// when
		notificationCacheService.deleteCache(memberId, scenarioId);

		// then
		verify(hashOperations).delete(cacheKey, scenarioId.toString());
		verify(valueOperations).set(eq(etagKey), anyString());
		verify(redisTemplate).expire(eq(etagKey), anyLong(), any(TimeUnit.class));
	}


	@Test
	void Given_ValidMember_When_DeleteMemberAllCache_Then_DeleteAllCache() {
		// given
		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);

		// when
		notificationCacheService.deleteMemberAllCache(memberId);

		// then
		verify(redisTemplate).delete(cacheKey);
		verify(redisTemplate).delete(etagKey);
	}


	@Test
	void Given_EmptyCacheData_When_GetScenariosNotificationCache_Then_ReturnEmptyList() {
		// given
		String etag = "1234567890";
		Map<Object, Object> cacheData = new HashMap<>();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn(etag);
		given(hashOperations.entries(cacheKey)).willReturn(cacheData);

		// when
		ScenarioNotificationListResponse result = notificationCacheService.getScenariosNotificationCache(memberId);

		// then
		assertThat(result.etag()).isEqualTo(etag);
		assertThat(result.scenarios()).isEmpty();
	}


	@Test
	void Given_EmptyDatabaseResponse_When_GetScenariosNotificationCache_Then_ReturnEmptyList() {
		// given
		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn(null);
		given(scenarioNotificationService.getScenarioNotifications(memberId)).willReturn(List.of());

		// when
		ScenarioNotificationListResponse result = notificationCacheService.getScenariosNotificationCache(memberId);

		// then
		assertThat(result.scenarios()).isEmpty();
		verify(scenarioNotificationService).getScenarioNotifications(memberId);
	}


	@Test
	void Given_RedisException_When_GetScenariosNotificationCache_Then_ThrowNotificationCacheException() {
		// given
		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		doThrow(new RuntimeException("Redis connection failed")).when(valueOperations).get(anyString());

		// when & then
		assertThatThrownBy(() -> notificationCacheService.getScenariosNotificationCache(memberId))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_RedisException_When_GetSingleScenarioNotificationCache_Then_ThrowNotificationCacheException() {
		// given
		doThrow(new RuntimeException("Redis connection failed")).when(valueOperations).get(anyString());

		// when & then
		assertThatThrownBy(() -> notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_RedisException_When_UpdateCache_Then_ThrowNotificationCacheException() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("에러 발생 루틴")
			.memo("에러 발생 메모")
			.notification(notification)
			.build();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");
		given(notificationConditionSelector.findNotificationCondition(notification)).willReturn(null);
		given(serializer.serializeCondition(null)).willReturn("serialized_condition");
		given(serializer.serialize(any())).willReturn("serialized_cache_data");
		doThrow(new RuntimeException("Redis connection failed")).when(hashOperations)
			.put(anyString(), anyString(), anyString());

		// when & then
		assertThatThrownBy(() -> notificationCacheService.updateCache(memberId, scenario))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_RedisException_When_DeleteCache_Then_ThrowNotificationCacheException() {
		// given
		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");
		doThrow(new RuntimeException("Redis connection failed")).when(hashOperations).delete(anyString(), anyString());

		// when & then
		assertThatThrownBy(() -> notificationCacheService.deleteCache(memberId, scenarioId))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_RedisException_When_DeleteMemberAllCache_Then_LogError() {
		// given
		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		doThrow(new RuntimeException("Redis connection failed")).when(redisTemplate).delete(anyString());

		// when & then
		notificationCacheService.deleteMemberAllCache(memberId);
		// 예외가 발생해도 로그만 남기고 정상 종료되어야 함
	}


	@Test
	void Given_NotificationCondition_When_UpdateCache_Then_IncludeCondition() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("조건 포함 루틴")
			.memo("조건 포함 메모")
			.notification(notification)
			.build();

		TimeNotificationResponse condition = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(12)
			.startMinute(30)
			.build();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");
		given(notificationConditionSelector.findNotificationCondition(notification)).willReturn(condition);
		given(serializer.serializeCondition(condition)).willReturn("serialized_condition");
		given(serializer.serialize(any())).willReturn("serialized_cache_data");

		// when
		notificationCacheService.updateCache(memberId, scenario);

		// then
		verify(notificationConditionSelector).findNotificationCondition(notification);
		verify(serializer).serializeCondition(condition);
		verify(hashOperations).put(eq(cacheKey), eq(scenarioId.toString()), eq("serialized_cache_data"));
	}


	@Test
	void Given_NotificationCondition_When_GetSingleScenarioNotificationCache_Then_IncludeCondition() {
		// given
		String cachedValue = "serialized_cache_data";
		NotificationCacheData notificationCacheData = NotificationCacheData.builder()
			.scenarioId(scenarioId)
			.scenarioName("조건 포함 루틴")
			.scenarioMemo("조건 포함 메모")
			.build();

		TimeNotificationResponse condition = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(12)
			.startMinute(30)
			.build();

		given(keyGenerator.generateNotificationCacheKey(memberId)).willReturn(cacheKey);
		given(keyGenerator.generateEtagKey(memberId)).willReturn(etagKey);
		given(valueOperations.get(etagKey)).willReturn("1234567890");
		given(hashOperations.get(cacheKey, scenarioId.toString())).willReturn(cachedValue);
		given(serializer.deserialize(cachedValue)).willReturn(notificationCacheData);
		given(serializer.parseCondition(notificationCacheData)).willReturn(condition);

		// when
		ScenarioNotificationResponse result =
			notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.scenarioId()).isEqualTo(scenarioId);
		assertThat(result.notificationCondition()).isEqualTo(condition);
	}

}
