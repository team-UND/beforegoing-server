package com.und.server.notification.dto.response;

import java.util.List;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Scenario notification response with notification active")
public record ScenarioNotificationResponse(

	@Schema(description = "Scenario id", example = "1")
	Long scenarioId,

	@Schema(description = "Scenario name", example = "Home out")
	String scenarioName,

	@Schema(description = "Scenario memo", example = "Item to carry")
	String memo,

	@Schema(description = "Notification id", example = "2")
	Long notificationId,

	@Schema(description = "Notification type", example = "TIME")
	NotificationType notificationType,

	@Schema(description = "Notification method type", example = "PUSH")
	NotificationMethodType notificationMethodType,

	@ArraySchema(
		uniqueItems = true,
		arraySchema = @Schema(
			description = "List of days in week when notification is active (0=Monday ... 6=Sunday)"),
		schema = @Schema(type = "integer", minimum = "0", maximum = "6")
	)
	@Schema(example = "[0,1,2,3,4,5,6]")
	List<Integer> daysOfWeekOrdinal,

	@Schema(description = "Notification condition, present only when active")
	NotificationConditionResponse notificationCondition

) {

	public static ScenarioNotificationResponse from(
		final NotificationCacheData notificationCacheData,
		final NotificationConditionResponse notificationConditionResponse
	) {
		return ScenarioNotificationResponse.builder()
			.scenarioId(notificationCacheData.scenarioId())
			.scenarioName(notificationCacheData.scenarioName())
			.memo(notificationCacheData.scenarioMemo())
			.notificationId(notificationCacheData.notificationId())
			.notificationType(notificationCacheData.notificationType())
			.notificationMethodType(notificationCacheData.notificationMethodType())
			.daysOfWeekOrdinal(notificationCacheData.daysOfWeekOrdinal())
			.notificationCondition(notificationConditionResponse)
			.build();
	}

	public static ScenarioNotificationResponse from(
		final Scenario scenario, final NotificationConditionResponse notificationConditionResponse
	) {
		Notification notification = scenario.getNotification();

		return ScenarioNotificationResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.notificationId(notification.getId())
			.notificationType(notification.getNotificationType())
			.notificationMethodType(notification.getNotificationMethodType())
			.daysOfWeekOrdinal(notification.getDaysOfWeekOrdinalList())
			.notificationCondition(notificationConditionResponse)
			.build();
	}

}
