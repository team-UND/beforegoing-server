package com.und.server.notification.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification response")
public record NotificationResponse(

	@Schema(description = "Notification id", example = "1")
	Long notificationId,

	@Schema(description = "Notification active status", example = "true")
	Boolean isActive,

	@Schema(description = "Notification type", example = "TIME")
	NotificationType notificationType,

	@Schema(description = "Notification method type", example = "PUSH")
	NotificationMethodType notificationMethodType,

	@Schema(description = "Whether the notification applies to every day of the week", example = "true")
	Boolean isEveryDay,

	@ArraySchema(
		uniqueItems = true,
		arraySchema = @Schema(
			description = "List of days in week when notification is active (0=Monday ... 6=Sunday)"),
		schema = @Schema(type = "integer", minimum = "0", maximum = "6")
	)
	@Schema(example = "[0,1,2,3,4,5,6]")
	List<Integer> dayOfWeekOrdinalList

) {

	public static NotificationResponse from(
		Notification notification,
		Boolean isEveryDay,
		List<Integer> dayOfWeekOrdinalList
	) {
		return NotificationResponse.builder()
			.notificationId(notification.getId())
			.isActive(notification.isActive())
			.notificationType(notification.getNotificationType())
			.notificationMethodType(notification.getNotificationMethodType())
			.isEveryDay(isEveryDay)
			.dayOfWeekOrdinalList(dayOfWeekOrdinalList)
			.build();
	}

}
