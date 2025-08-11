package com.und.server.notification.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.constants.NotifMethodType;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.entity.Notification;

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
public class NotificationResponse {

	private Long notificationId;
	private Boolean isActive;
	private NotifType notificationType;
	private NotifMethodType notificationMethodType;
	private Boolean isEveryDay;
	private List<Integer> dayOfWeekOrdinalList;

	public static NotificationResponse of(Notification notification) {
		return NotificationResponse.builder()
			.notificationId(notification.getId())
			.isActive(notification.isActive())
			.notificationType(notification.getNotifType())
			.notificationMethodType(notification.getNotifMethodType())
			.build();
	}

}
