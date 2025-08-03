package com.und.server.notification.dto;

import java.util.List;

import com.und.server.notification.entity.TimeNotif;

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
public class TimeNotifResponse extends NotificationDetailResponse {

	private Integer hour;
	private Integer minute;


	public static NotificationDetailResponse of(TimeNotif timeNotif) {
		return TimeNotifResponse.builder()
			.hour(timeNotif.getHour())
			.minute(timeNotif.getMinute())
			.build();
	}

	public static List<NotificationDetailResponse> listOf(List<TimeNotif> timeNotifList) {
		return timeNotifList.stream()
			.map(TimeNotifResponse::of)
			.toList();
	}

}
