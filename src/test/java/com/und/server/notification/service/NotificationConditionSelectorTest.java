package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationErrorResult;

@ExtendWith(MockitoExtension.class)
class NotificationConditionSelectorTest {

	@Mock
	private NotificationConditionService timeNotificationService;

	@Mock
	private NotificationConditionService locationNotificationService;

	@Mock
	private Notification notification;

	@Mock
	private NotificationConditionRequest conditionRequest;

	@InjectMocks
	private NotificationConditionSelector selector;

	private List<NotificationConditionService> services;


	@BeforeEach
	void setUp() {
		services = Arrays.asList(timeNotificationService, locationNotificationService);
		selector = new NotificationConditionSelector(services);
	}


	@Test
	void Given_SupportedNotificationType_When_FindNotifByNotifType_Then_ReturnNotificationInfoDto() {
		// given
		NotifType notifType = NotifType.TIME;
		NotificationInfoDto expectedDto = new NotificationInfoDto(true, List.of(0, 1, 2), null);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);
		when(timeNotificationService.findNotifByNotifType(notification)).thenReturn(expectedDto);

		// when
		NotificationInfoDto result = selector.findNotifByNotifType(notification);

		// then
		assertThat(result).isEqualTo(expectedDto);
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).findNotifByNotifType(notification);
	}


	@Test
	void Given_UnsupportedNotificationType_When_FindNotifByNotifType_Then_ThrowServerException() {
		// given
		NotifType notifType = NotifType.LOCATION;

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.findNotifByNotifType(notification))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIF);
	}


	@Test
	void Given_SupportedNotificationType_When_AddNotif_Then_InvokeService() {
		// given
		NotifType notifType = NotifType.TIME;
		List<Integer> dayOfWeekList = List.of(0, 1, 2);
		TimeNotificationRequest timeRequest = new TimeNotificationRequest(9, 0);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);

		// when
		selector.addNotif(notification, dayOfWeekList, timeRequest);

		// then
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).addNotif(notification, dayOfWeekList, timeRequest);
	}


	@Test
	void Given_UnsupportedNotificationType_When_AddNotif_Then_ThrowServerException() {
		// given
		NotifType notifType = NotifType.LOCATION;
		List<Integer> dayOfWeekList = List.of(0, 1, 2);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.addNotif(notification, dayOfWeekList, conditionRequest))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIF);
	}


	@Test
	void Given_SupportedNotificationType_When_DeleteNotif_Then_InvokeService() {
		// given
		NotifType notifType = NotifType.TIME;
		Long notificationId = 1L;

		when(timeNotificationService.supports(notifType)).thenReturn(true);

		// when
		selector.deleteNotif(notifType, notificationId);

		// then
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).deleteNotif(notificationId);
	}


	@Test
	void Given_UnsupportedNotificationType_When_DeleteNotif_Then_ThrowServerException() {
		// given
		NotifType notifType = NotifType.LOCATION;
		Long notificationId = 1L;

		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.deleteNotif(notifType, notificationId))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIF);
	}


	@Test
	void Given_SupportedNotificationType_When_UpdateNotif_Then_InvokeService() {
		// given
		NotifType notifType = NotifType.TIME;
		List<Integer> dayOfWeekList = List.of(0, 1, 2);
		TimeNotificationRequest timeRequest = new TimeNotificationRequest(9, 0);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);

		// when
		selector.updateNotif(notification, dayOfWeekList, timeRequest);

		// then
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).updateNotif(notification, dayOfWeekList, timeRequest);
	}


	@Test
	void Given_UnsupportedNotificationType_When_UpdateNotif_Then_ThrowServerException() {
		// given
		NotifType notifType = NotifType.LOCATION;
		List<Integer> dayOfWeekList = List.of(0, 1, 2);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.updateNotif(notification, dayOfWeekList, conditionRequest))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIF);
	}


	@Test
	void Given_MultipleServices_When_FirstServiceSupports_Then_UseFirstService() {
		// given
		NotifType notifType = NotifType.TIME;
		NotificationInfoDto expectedDto = new NotificationInfoDto(true, List.of(0, 1, 2), null);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);
		when(timeNotificationService.findNotifByNotifType(notification)).thenReturn(expectedDto);

		// when
		NotificationInfoDto result = selector.findNotifByNotifType(notification);

		// then
		assertThat(result).isEqualTo(expectedDto);
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).findNotifByNotifType(notification);
		// 두 번째 서비스는 호출되지 않아야 함
		verify(locationNotificationService, org.mockito.Mockito.never()).supports(any());
	}


	@Test
	void Given_MultipleServices_When_FirstServiceNotSupports_Then_UseSecondService() {
		// given
		NotifType notifType = NotifType.LOCATION;
		NotificationInfoDto expectedDto = new NotificationInfoDto(false, List.of(0, 1), null);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(true);
		when(locationNotificationService.findNotifByNotifType(notification)).thenReturn(expectedDto);

		// when
		NotificationInfoDto result = selector.findNotifByNotifType(notification);

		// then
		assertThat(result).isEqualTo(expectedDto);
		verify(timeNotificationService).supports(notifType);
		verify(locationNotificationService).supports(notifType);
		verify(locationNotificationService).findNotifByNotifType(notification);
	}

}
