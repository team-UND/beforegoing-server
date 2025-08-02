package com.und.server.notification.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeNotifResponse extends NotificationDetailResponse {

	private Long timeNotificationId;
	private Boolean isEveryDay;
	private Integer dayOfWeekOrdinal;
	private Integer hour;
	private Integer minute;


	public static NotificationDetailResponse of(TimeNotif timeNotif) {
		Integer dayOfWeekOrdinal = timeNotif.getDayOfWeek() != null ? timeNotif.getDayOfWeek().ordinal() : null;
		Integer hour = timeNotif.getHour();
		Integer minute = timeNotif.getMinute();

		boolean isEveryDay = dayOfWeekOrdinal == null && hour == null && minute == null;

		return TimeNotifResponse.builder()
			.timeNotificationId(timeNotif.getId())
			.isEveryDay(isEveryDay)
			.dayOfWeekOrdinal(dayOfWeekOrdinal)
			.hour(hour)
			.minute(minute)
			.build();
	}

	public static List<NotificationDetailResponse> listOf(List<TimeNotif> timeNotifList) {
		return timeNotifList.stream()
			.map(TimeNotifResponse::of)
			.toList();
	}

}
