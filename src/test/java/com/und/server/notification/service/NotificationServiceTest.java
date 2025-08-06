package com.und.server.notification.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotifMethodType;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationDayOfWeekRequest;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationDayOfWeekResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationConditionSelector notificationResolverSelector;


	@Test
	void Given_Notification_When_FindNotificationDetails_Then_ReturnNotificationInfoDto() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		TimeNotificationResponse timeNotifResponse = TimeNotificationResponse.builder()
			.hour(9)
			.minute(30)
			.build();

		NotificationInfoDto expected = new NotificationInfoDto(
			true,
			List.of(new NotificationDayOfWeekResponse(1L, 1)),
			timeNotifResponse
		);

		when(notificationResolverSelector.findNotifByNotifType(notification))
			.thenReturn(expected);

		// when
		NotificationInfoDto result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEveryDay()).isTrue();
		assertThat(result.notificationCondition()).isInstanceOf(TimeNotificationResponse.class);

		TimeNotificationResponse actualDetail = (TimeNotificationResponse) result.notificationCondition();
		assertThat(actualDetail.getHour()).isEqualTo(9);
		assertThat(actualDetail.getMinute()).isEqualTo(30);
	}


	@Test
	void Given_NotificationRequestAndConditionRequest_When_AddNotification_Then_SaveNotificationAndAddDetail() {
		// given
		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.ALARM)
			.dayOfWeekOrdinalList(List.of(
				new NotificationDayOfWeekRequest(1L, 1),
				new NotificationDayOfWeekRequest(2L, 2)
			))
			.build();

		TimeNotificationRequest timeRequest = new TimeNotificationRequest(8, 45);

		Notification expectedEntity = Notification.builder()
			.isActive(notifRequest.getIsActive())
			.notifType(notifRequest.getNotificationType())
			.notifMethodType(notifRequest.getNotificationMethodType())
			.build();

		when(notificationRepository.save(any(Notification.class)))
			.thenReturn(expectedEntity);

		// when
		Notification result = notificationService.addNotification(notifRequest, timeRequest);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getIsActive()).isTrue();
		assertThat(result.getNotifType()).isEqualTo(NotifType.TIME);
		assertThat(result.getNotifMethodType()).isEqualTo(NotifMethodType.ALARM);

		List<Integer> expectedDays = List.of(1, 2);

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationResolverSelector).addNotif(eq(result), eq(expectedDays), eq(timeRequest));
	}

}
