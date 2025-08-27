package com.und.server.notification.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.exception.NotificationCacheException;


@ExtendWith(MockitoExtension.class)
class NotificationCacheSerializerTest {

	@Mock
	private ObjectMapper objectMapper;

	private NotificationCacheSerializer notificationCacheSerializer;

	@BeforeEach
	void setUp() {
		notificationCacheSerializer = new NotificationCacheSerializer(objectMapper);
	}


	@Test
	void Given_ValidNotificationCacheData_When_Serialize_Then_ReturnJsonString() throws JsonProcessingException {
		// given
		NotificationCacheData cacheData = NotificationCacheData.builder()
			.scenarioId(1L)
			.scenarioName("테스트 루틴")
			.scenarioMemo("테스트 메모")
			.notificationId(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(1, 2, 3, 4, 5))
			.conditionJson("{\"notificationType\":\"TIME\",\"startHour\":9,\"startMinute\":30}")
			.build();

		String expectedJson = "{\"scenarioId\":1,\"scenarioName\":\"테스트 루틴\"}";
		when(objectMapper.writeValueAsString(cacheData)).thenReturn(expectedJson);

		// when
		String result = notificationCacheSerializer.serialize(cacheData);

		// then
		assertThat(result).isEqualTo(expectedJson);
	}


	@Test
	void Given_ValidJson_When_Deserialize_Then_ReturnNotificationCacheData() throws JsonProcessingException {
		// given
		String json = "{\"scenarioId\":1,\"scenarioName\":\"테스트 루틴\"}";
		NotificationCacheData expectedData = NotificationCacheData.builder()
			.scenarioId(1L)
			.scenarioName("테스트 루틴")
			.build();

		when(objectMapper.readValue(json, NotificationCacheData.class)).thenReturn(expectedData);

		// when
		NotificationCacheData result = notificationCacheSerializer.deserialize(json);

		// then
		assertThat(result).isEqualTo(expectedData);
	}


	@Test
	void Given_ValidNotificationCacheData_When_ParseCondition_Then_ReturnNotificationConditionResponse()
		throws JsonProcessingException {
		// given
		NotificationCacheData cacheData = NotificationCacheData.builder()
			.conditionJson("{\"notificationType\":\"TIME\",\"startHour\":9,\"startMinute\":30}")
			.build();

		TimeNotificationResponse expectedCondition = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		when(objectMapper.readValue(cacheData.conditionJson(), NotificationConditionResponse.class))
			.thenReturn(expectedCondition);

		// when
		NotificationConditionResponse result = notificationCacheSerializer.parseCondition(cacheData);

		// then
		assertThat(result).isEqualTo(expectedCondition);
	}


	@Test
	void Given_ValidNotificationConditionResponse_When_SerializeCondition_Then_ReturnJsonString()
		throws JsonProcessingException {
		// given
		TimeNotificationResponse condition = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		String expectedJson = "{\"notificationType\":\"TIME\",\"startHour\":9,\"startMinute\":30}";
		when(objectMapper.writeValueAsString(condition)).thenReturn(expectedJson);

		// when
		String result = notificationCacheSerializer.serializeCondition(condition);

		// then
		assertThat(result).isEqualTo(expectedJson);
	}


	@Test
	void Given_JsonProcessingException_When_Serialize_Then_ThrowNotificationCacheException()
		throws JsonProcessingException {
		// given
		NotificationCacheData cacheData = NotificationCacheData.builder()
			.scenarioId(1L)
			.scenarioName("테스트 루틴")
			.build();

		doThrow(new JsonProcessingException("Serialization failed") {
		}).when(objectMapper).writeValueAsString(any());

		// when & then
		assertThatThrownBy(() -> notificationCacheSerializer.serialize(cacheData))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_JsonProcessingException_When_Deserialize_Then_ThrowNotificationCacheException()
		throws JsonProcessingException {
		// given
		String json = "invalid json";

		doThrow(new JsonProcessingException("Deserialization failed") {
		}).when(objectMapper).readValue(anyString(), eq(NotificationCacheData.class));

		// when & then
		assertThatThrownBy(() -> notificationCacheSerializer.deserialize(json))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_JsonProcessingException_When_ParseCondition_Then_ThrowNotificationCacheException()
		throws JsonProcessingException {
		// given
		NotificationCacheData cacheData = NotificationCacheData.builder()
			.conditionJson("invalid json")
			.build();

		doThrow(new JsonProcessingException("Condition parsing failed") {
		}).when(objectMapper).readValue(anyString(), eq(NotificationConditionResponse.class));

		// when & then
		assertThatThrownBy(() -> notificationCacheSerializer.parseCondition(cacheData))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_JsonProcessingException_When_SerializeCondition_Then_ThrowNotificationCacheException()
		throws JsonProcessingException {
		// given
		TimeNotificationResponse condition = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		doThrow(new JsonProcessingException("Condition serialization failed") {
		}).when(objectMapper).writeValueAsString(any());

		// when & then
		assertThatThrownBy(() -> notificationCacheSerializer.serializeCondition(condition))
			.isInstanceOf(NotificationCacheException.class);
	}


	@Test
	void Given_NullNotificationCacheData_When_Serialize_Then_ReturnJsonString() throws JsonProcessingException {
		// given
		NotificationCacheData cacheData = null;
		String expectedJson = "null";
		when(objectMapper.writeValueAsString(cacheData)).thenReturn(expectedJson);

		// when
		String result = notificationCacheSerializer.serialize(cacheData);

		// then
		assertThat(result).isEqualTo(expectedJson);
	}


	@Test
	void Given_NullJson_When_Deserialize_Then_ReturnNotificationCacheData() throws JsonProcessingException {
		// given
		String json = null;
		NotificationCacheData expectedData = NotificationCacheData.builder()
			.scenarioId(1L)
			.scenarioName("테스트 루틴")
			.build();

		when(objectMapper.readValue(json, NotificationCacheData.class)).thenReturn(expectedData);

		// when
		NotificationCacheData result = notificationCacheSerializer.deserialize(json);

		// then
		assertThat(result).isEqualTo(expectedData);
	}

}
