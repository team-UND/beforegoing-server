package com.und.server.notification.dto.request;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Time notification detail condition request")
public record TimeNotificationRequest(

	@Schema(
		description = "Time notification type",
		example = "time",
		defaultValue = "time",
		allowableValues = {"time"},
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotNull
	NotificationType notificationType,

	@Schema(description = "hour, 24-hour format", example = "12")
	@NotNull(message = "Hour must not be null")
	@Min(value = 0, message = "Hour must be between 0 and 23")
	@Max(value = 23, message = "Hour must be between 0 and 23")
	Integer startHour,

	@Schema(description = "minute", example = "58")
	@NotNull(message = "Minute must not be null")
	@Min(value = 0, message = "Minute must be between 0 and 59")
	@Max(value = 59, message = "Minute must be between 0 and 59")
	Integer startMinute

) implements NotificationConditionRequest {

	public TimeNotificationRequest {
		if (notificationType == null) {
			notificationType = NotificationType.TIME;
		}
	}

	public TimeNotification toEntity(final Notification notification) {
		return TimeNotification.builder()
			.notification(notification)
			.startHour(startHour)
			.startMinute(startMinute)
			.build();
	}

}
