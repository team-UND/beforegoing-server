package com.und.server.notification.dto.cache;

import java.util.List;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;

import lombok.Builder;

@Builder
public record NotificationCacheData(

	Long scenarioId,
	String scenarioName,
	String scenarioMemo,
	Long notificationId,
	NotificationType notificationType,
	NotificationMethodType notificationMethodType,
	List<Integer> daysOfWeek,
	String conditionJson

) {

	public static NotificationCacheData from(
		final ScenarioNotificationResponse scenarioNotificationResponse,
		final String serializedCondition
	) {
		return NotificationCacheData.builder()
			.scenarioId(scenarioNotificationResponse.scenarioId())
			.scenarioName(scenarioNotificationResponse.scenarioName())
			.scenarioMemo(scenarioNotificationResponse.memo())
			.notificationId(scenarioNotificationResponse.notificationId())
			.notificationType(scenarioNotificationResponse.notificationType())
			.notificationMethodType(scenarioNotificationResponse.notificationMethodType())
			.daysOfWeek(scenarioNotificationResponse.daysOfWeek())
			.conditionJson(serializedCondition)
			.build();
	}

	public static NotificationCacheData from(
		final Scenario scenario,
		final String serializedCondition
	) {
		Notification notification = scenario.getNotification();

		return NotificationCacheData.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.scenarioMemo(scenario.getMemo())
			.notificationId(notification.getId())
			.notificationType(notification.getNotificationType())
			.notificationMethodType(notification.getNotificationMethodType())
			.daysOfWeek(notification.getDaysOfWeekOrdinalList())
			.conditionJson(serializedCondition)
			.build();
	}

}
