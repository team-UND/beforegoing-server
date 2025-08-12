package com.und.server.notification.dto.response;

import com.und.server.notification.entity.TimeNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Time notification detail condition response")
public record TimeNotificationResponse(

	@Schema(description = "hour", example = "12")
	Integer startHour,

	@Schema(description = "minute", example = "58")
	Integer startMinute

) implements NotificationConditionResponse {

	public static NotificationConditionResponse from(TimeNotification timeNotification) {
		return TimeNotificationResponse.builder()
			.startHour(timeNotification.getStartHour())
			.startMinute(timeNotification.getStartMinute())
			.build();
	}

}
