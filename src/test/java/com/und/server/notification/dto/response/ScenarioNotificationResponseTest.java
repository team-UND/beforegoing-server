package com.und.server.notification.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;


class ScenarioNotificationResponseTest {

	private final Long scenarioId = 1L;
	private final String scenarioName = "테스트 루틴";
	private final String memo = "테스트 메모";
	private final Long notificationId = 2L;
	private final NotificationType notificationType = NotificationType.TIME;
	private final NotificationMethodType notificationMethodType = NotificationMethodType.PUSH;
	private final List<Integer> daysOfWeekOrdinal = List.of(1, 2, 3, 4, 5);


	@Test
	void Given_ValidNotificationCacheData_When_From_Then_ReturnScenarioNotificationResponse() {
		// given
		NotificationCacheData cacheData = NotificationCacheData.builder()
			.scenarioId(scenarioId)
			.scenarioName(scenarioName)
			.scenarioMemo(memo)
			.notificationId(notificationId)
			.notificationType(notificationType)
			.notificationMethodType(notificationMethodType)
			.daysOfWeekOrdinal(daysOfWeekOrdinal)
			.conditionJson("{\"notificationType\":\"TIME\",\"startHour\":9,\"startMinute\":30}")
			.build();

		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		// when
		ScenarioNotificationResponse result = ScenarioNotificationResponse.from(cacheData, timeNotificationResponse);

		// then
		assertThat(result.scenarioId()).isEqualTo(scenarioId);
		assertThat(result.scenarioName()).isEqualTo(scenarioName);
		assertThat(result.memo()).isEqualTo(memo);
		assertThat(result.notificationId()).isEqualTo(notificationId);
		assertThat(result.notificationType()).isEqualTo(notificationType);
		assertThat(result.notificationMethodType()).isEqualTo(notificationMethodType);
		assertThat(result.daysOfWeekOrdinal()).isEqualTo(daysOfWeekOrdinal);
		assertThat(result.notificationCondition()).isEqualTo(timeNotificationResponse);
	}


	@Test
	void Given_ValidScenarioAndNotificationCondition_When_From_Then_ReturnScenarioNotificationResponse() {
		// given
		Notification notification = Notification.builder()
			.id(notificationId)
			.notificationType(notificationType)
			.notificationMethodType(notificationMethodType)
			.daysOfWeek("1,2,3,4,5")
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName(scenarioName)
			.memo(memo)
			.notification(notification)
			.build();

		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(10)
			.startMinute(0)
			.build();

		// when
		ScenarioNotificationResponse result = ScenarioNotificationResponse.from(scenario, timeNotificationResponse);

		// then
		assertThat(result.scenarioId()).isEqualTo(scenarioId);
		assertThat(result.scenarioName()).isEqualTo(scenarioName);
		assertThat(result.memo()).isEqualTo(memo);
		assertThat(result.notificationId()).isEqualTo(notificationId);
		assertThat(result.notificationType()).isEqualTo(notificationType);
		assertThat(result.notificationMethodType()).isEqualTo(notificationMethodType);
		assertThat(result.daysOfWeekOrdinal()).isEqualTo(daysOfWeekOrdinal);
		assertThat(result.notificationCondition()).isEqualTo(timeNotificationResponse);
	}


	@Test
	void Given_ValidBuilder_When_Build_Then_ReturnScenarioNotificationResponse() {
		// given
		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(12)
			.startMinute(30)
			.build();

		// when
		ScenarioNotificationResponse result = ScenarioNotificationResponse.builder()
			.scenarioId(scenarioId)
			.scenarioName(scenarioName)
			.memo(memo)
			.notificationId(notificationId)
			.notificationType(notificationType)
			.notificationMethodType(notificationMethodType)
			.daysOfWeekOrdinal(daysOfWeekOrdinal)
			.notificationCondition(timeNotificationResponse)
			.build();

		// then
		assertThat(result.scenarioId()).isEqualTo(scenarioId);
		assertThat(result.scenarioName()).isEqualTo(scenarioName);
		assertThat(result.memo()).isEqualTo(memo);
		assertThat(result.notificationId()).isEqualTo(notificationId);
		assertThat(result.notificationType()).isEqualTo(notificationType);
		assertThat(result.notificationMethodType()).isEqualTo(notificationMethodType);
		assertThat(result.daysOfWeekOrdinal()).isEqualTo(daysOfWeekOrdinal);
		assertThat(result.notificationCondition()).isEqualTo(timeNotificationResponse);
	}


	@Test
	void Given_EmptyDaysOfWeekOrdinal_When_FromNotificationCacheData_Then_ReturnEmptyList() {
		// given
		NotificationCacheData cacheData = NotificationCacheData.builder()
			.scenarioId(scenarioId)
			.scenarioName(scenarioName)
			.scenarioMemo(memo)
			.notificationId(notificationId)
			.notificationType(notificationType)
			.notificationMethodType(notificationMethodType)
			.daysOfWeekOrdinal(List.of())
			.conditionJson("{\"notificationType\":\"TIME\",\"startHour\":9,\"startMinute\":30}")
			.build();

		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		// when
		ScenarioNotificationResponse result = ScenarioNotificationResponse.from(cacheData, timeNotificationResponse);

		// then
		assertThat(result.daysOfWeekOrdinal()).isEmpty();
		assertThat(result.scenarioId()).isEqualTo(scenarioId);
		assertThat(result.scenarioName()).isEqualTo(scenarioName);
	}


	@Test
	void Given_DifferentNotificationMethodType_When_FromScenario_Then_ReturnCorrectMethodType() {
		// given
		Notification notification = Notification.builder()
			.id(notificationId)
			.notificationType(notificationType)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeek("1,2,3,4,5")
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName(scenarioName)
			.memo(memo)
			.notification(notification)
			.build();

		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(15)
			.startMinute(45)
			.build();

		// when
		ScenarioNotificationResponse result = ScenarioNotificationResponse.from(scenario, timeNotificationResponse);

		// then
		assertThat(result.notificationMethodType()).isEqualTo(NotificationMethodType.ALARM);
		assertThat(result.scenarioId()).isEqualTo(scenarioId);
		assertThat(result.scenarioName()).isEqualTo(scenarioName);
	}

}
