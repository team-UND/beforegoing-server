package com.und.server.notification.dto.response;

import java.util.List;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "시나리오별 알림 응답")
public record ScenarioNotificationResponse(

	@Schema(description = "시나리오 ID", example = "201")
	Long scenarioId,

	@Schema(description = "시나리오 이름", example = "아침 루틴")
	String scenarioName,

	@Schema(description = "시나리오 메모", example = "출근 전 해야 할 일")
	String memo,

	@Schema(description = "알림 ID", example = "101")
	Long notificationId,

	@Schema(description = "알림 타입 (TIME / LOCATION)", example = "TIME")
	NotificationType notificationType,

	@Schema(description = "알림 방식 타입 (PUSH / LOCAL 등)", example = "PUSH")
	NotificationMethodType notificationMethodType,

	@Schema(description = "알림 요일 목록 (0=월요일, 1=화요일, ..., 6=일요일)", example = "[1, 3, 5]")
	List<Integer> daysOfWeek,

	@Schema(description = "알림 조건 (타입별 구조 다름)")
	NotificationConditionResponse notificationCondition

) {

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
			.daysOfWeek(notification.getDaysOfWeekOrdinalList())
			.notificationCondition(notificationConditionResponse)
			.build();
	}

}
