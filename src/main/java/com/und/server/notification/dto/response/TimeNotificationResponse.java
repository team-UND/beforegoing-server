package com.und.server.notification.dto.response;

import com.und.server.notification.entity.TimeNotification;

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

	private Integer hour;
	private Integer minute;


	public static NotificationConditionResponse of(TimeNotification timeNotif) {
		return TimeNotificationResponse.builder()
			.hour(timeNotif.getHour())
			.minute(timeNotif.getMinute())
			.build();
	}

}
