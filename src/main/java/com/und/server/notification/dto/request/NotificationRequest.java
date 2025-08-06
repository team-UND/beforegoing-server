package com.und.server.notification.dto.request;

import java.util.List;

import com.und.server.notification.constants.NotifMethodType;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.entity.Notification;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class NotificationRequest {

	private Long notificationId;

	@NotNull(message = "isActive must not be null")
	private Boolean isActive;

	@NotNull(message = "notificationType must not be null")
	private NotifType notificationType;

	@NotNull(message = "notificationMethod must not be null")
	private NotifMethodType notificationMethodType;

	@Size(max = 7, message = "DayOfWeek list must contain at most 7 items")
	private List<NotificationDayOfWeekRequest> dayOfWeekOrdinalList;


	public Notification toEntity() {
		return Notification.builder()
			.isActive(isActive)
			.notifType(notificationType)
			.notifMethodType(notificationMethodType)
			.build();
	}

}
