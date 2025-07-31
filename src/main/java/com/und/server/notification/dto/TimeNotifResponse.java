package com.und.server.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.entity.NotifType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeNotifResponse implements NotificationResponse{

	private Long notificationId;
	private Boolean isEveryDay;
	private Integer dayOfWeekOrdinal;
	private Integer hour;
	private Integer minute;


	@Override
	public NotifType getNotificationType() {
		return NotifType.TIME;
	}

}
