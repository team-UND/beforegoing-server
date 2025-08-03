package com.und.server.notification.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NofitDayOfWeekResponse;
import com.und.server.notification.dto.TimeNotifResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.dto.NotificationInfoDto;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationResolverSelector notificationResolverSelector;


	@Test
	void Given_Notification_When_FindNotificationDetails_Then_ReturnNotificationInfoDto() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		TimeNotifResponse timeNotifResponse = TimeNotifResponse.builder()
			.hour(9)
			.minute(30)
			.build();

		NotificationInfoDto expected = new NotificationInfoDto(
			true,
			List.of(new NofitDayOfWeekResponse(1L, 1)),
			timeNotifResponse
		);

		Mockito.when(notificationResolverSelector.resolve(notification))
			.thenReturn(expected);

		// when
		NotificationInfoDto result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEveryDay()).isTrue();
		assertThat(result.notificationDetail()).isInstanceOf(TimeNotifResponse.class);

		TimeNotifResponse actualDetail = (TimeNotifResponse) result.notificationDetail();
		assertThat(actualDetail.getHour()).isEqualTo(9);
		assertThat(actualDetail.getMinute()).isEqualTo(30);
	}

}
