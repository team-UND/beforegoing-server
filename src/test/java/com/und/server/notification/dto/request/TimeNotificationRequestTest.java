package com.und.server.notification.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;

import org.junit.jupiter.api.Test;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;

class TimeNotificationRequestTest {

	@Test
	void constructor_nullType_defaultsToTime() {
		TimeNotificationRequest req = TimeNotificationRequest.builder()
			.notificationType(null)
			.startHour(10)
			.startMinute(15)
			.build();

		assertThat(req.notificationType()).isEqualTo(NotificationType.TIME);
	}

	@Test
	void toEntity_mapsFields() {
		Notification notification = Notification.builder().id(1L).build();

		TimeNotificationRequest req = TimeNotificationRequest.builder()
			.notificationType(NotificationType.TIME)
			.startHour(8)
			.startMinute(45)
			.build();

		TimeNotification entity = req.toEntity(notification, DayOfWeek.MONDAY);

		assertThat(entity.getNotification()).isEqualTo(notification);
		assertThat(entity.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
		assertThat(entity.getStartHour()).isEqualTo(8);
		assertThat(entity.getStartMinute()).isEqualTo(45);
	}

}


