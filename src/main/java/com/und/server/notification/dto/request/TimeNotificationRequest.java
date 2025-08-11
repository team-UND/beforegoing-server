package com.und.server.notification.dto.request;

import java.time.DayOfWeek;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class TimeNotificationRequest implements NotificationConditionRequest {

	@Schema(
		description = "알림 타입",
		example = "TIME",
		defaultValue = "TIME",
		allowableValues = {"TIME"},
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotNull
	@Builder.Default
	private NotifType notificationType = NotifType.TIME;

	@NotNull(message = "Hour must not be null")
	@Min(value = 0, message = "Hour must be between 0 and 23")
	@Max(value = 23, message = "Hour must be between 0 and 23")
	private Integer startHour;

	@NotNull(message = "Minute must not be null")
	@Min(value = 0, message = "Minute must be between 0 and 59")
	@Max(value = 59, message = "Minute must be between 0 and 59")
	private Integer startMinute;

	public TimeNotification toEntity(Notification notification, DayOfWeek dayOfWeek) {
		return TimeNotification.builder()
			.notification(notification)
			.dayOfWeek(dayOfWeek)
			.startHour(startHour)
			.startMinute(startMinute)
			.build();
	}

}
