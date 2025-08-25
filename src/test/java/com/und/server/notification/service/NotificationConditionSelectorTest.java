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
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
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
	void Given_SupportedNotificationType_When_FindNotificationInfoByType_Then_ReturnNotificationInfoDto() {
		// given
		NotificationType notifType = NotificationType.TIME;
		NotificationConditionResponse expectedDto = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);
		when(timeNotificationService.findNotificationInfoByType(notification)).thenReturn(expectedDto);

		// when
		NotificationConditionResponse result = selector.findNotificationCondition(notification);

		// then
		assertThat(result).isEqualTo(expectedDto);
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).findNotificationInfoByType(notification);
	}


	@Test
	void Given_UnsupportedNotificationType_When_FindNotificationInfoByType_Then_ThrowServerException() {
		// given
		NotificationType notifType = NotificationType.LOCATION;

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.findNotificationCondition(notification))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIFICATION);
	}


	@Test
	void Given_SupportedNotificationType_When_AddNotificationCondition_Then_InvokeService() {
		// given
		NotificationType notifType = NotificationType.TIME;
		TimeNotificationRequest timeRequest = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);

		// when
		selector.addNotificationCondition(notification, timeRequest);

		// then
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).addNotificationCondition(notification, timeRequest);
	}


	@Test
	void Given_UnsupportedNotificationType_When_AddNotificationCondition_Then_ThrowServerException() {
		// given
		NotificationType notifType = NotificationType.LOCATION;

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.addNotificationCondition(notification, conditionRequest))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIFICATION);
	}


	@Test
	void Given_SupportedNotificationType_When_DeleteNotificationCondition_Then_InvokeService() {
		// given
		NotificationType notifType = NotificationType.TIME;
		Long notificationId = 1L;

		when(timeNotificationService.supports(notifType)).thenReturn(true);

		// when
		selector.deleteNotificationCondition(notifType, notificationId);

		// then
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).deleteNotificationCondition(notificationId);
	}


	@Test
	void Given_UnsupportedNotificationType_When_DeleteNotificationCondition_Then_ThrowServerException() {
		// given
		NotificationType notifType = NotificationType.LOCATION;
		Long notificationId = 1L;

		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.deleteNotificationCondition(notifType, notificationId))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIFICATION);
	}


	@Test
	void Given_SupportedNotificationType_When_UpdateNotificationCondition_Then_InvokeService() {
		// given
		NotificationType notifType = NotificationType.TIME;
		TimeNotificationRequest timeRequest = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);

		// when
		selector.updateNotificationCondition(notification, timeRequest);

		// then
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).updateNotificationCondition(notification, timeRequest);
	}


	@Test
	void Given_UnsupportedNotificationType_When_UpdateNotificationCondition_Then_ThrowServerException() {
		// given
		NotificationType notifType = NotificationType.LOCATION;

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.updateNotificationCondition(notification, conditionRequest))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", NotificationErrorResult.UNSUPPORTED_NOTIFICATION);
	}


	@Test
	void Given_MultipleServices_When_FirstServiceSupports_Then_UseFirstService() {
		// given
		NotificationType notifType = NotificationType.TIME;
		NotificationConditionResponse expectedDto = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);
		when(timeNotificationService.findNotificationInfoByType(notification)).thenReturn(expectedDto);

		// when
		NotificationConditionResponse result = selector.findNotificationCondition(notification);

		// then
		assertThat(result).isEqualTo(expectedDto);
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).findNotificationInfoByType(notification);
		verify(locationNotificationService, org.mockito.Mockito.never()).supports(any());
	}


	@Test
	void Given_MultipleServices_When_FirstServiceNotSupports_Then_UseSecondService() {
		// given
		NotificationType notifType = NotificationType.LOCATION;
		NotificationConditionResponse expectedDto = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		when(notification.getNotificationType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(false);
		when(locationNotificationService.supports(notifType)).thenReturn(true);
		when(locationNotificationService.findNotificationInfoByType(notification)).thenReturn(expectedDto);

		// when
		NotificationConditionResponse result = selector.findNotificationCondition(notification);

		// then
		assertThat(result).isEqualTo(expectedDto);
		verify(timeNotificationService).supports(notifType);
		verify(locationNotificationService).supports(notifType);
		verify(locationNotificationService).findNotificationInfoByType(notification);
	}

}
