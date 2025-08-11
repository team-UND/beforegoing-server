package com.und.server.notification.dto.response;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.entity.TimeNotification;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class TimeNotificationResponse implements NotificationConditionResponse {

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
	private Integer startHour;
	private Integer startMinute;

	public static NotificationConditionResponse of(TimeNotification timeNotif) {
		return TimeNotificationResponse.builder()
			.startHour(timeNotif.getStartHour())
			.startMinute(timeNotif.getStartMinute())
			.build();
	}

}
