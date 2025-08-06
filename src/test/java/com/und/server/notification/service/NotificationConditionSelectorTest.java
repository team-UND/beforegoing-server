package com.und.server.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationErrorResult;

@ExtendWith(MockitoExtension.class)
class NotificationConditionSelectorTest {

	@InjectMocks
	private NotificationConditionSelector selector;

	@Mock
	private NotificationConditionService timeNotificationService;

	@Mock
	private NotificationConditionService anotherService;

	@Mock
	private Notification notification;

	@Mock
	private NotificationConditionRequest request;


	@BeforeEach
	void setUp() {
		List<NotificationConditionService> services = Arrays.asList(timeNotificationService, anotherService);
		selector = new NotificationConditionSelector(services);
	}


	@Test
	void Given_Notification_When_FindNotifByNotifType_Then_ReturnNotificationInfoDto() {
		// given
		NotifType notifType = NotifType.TIME;
		NotificationInfoDto expectedDto = new NotificationInfoDto(true, List.of(), null);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);
		when(timeNotificationService.findNotifByNotifType(notification)).thenReturn(expectedDto);

		// when
		NotificationInfoDto result = selector.findNotifByNotifType(notification);

		// then
		assertNotNull(result);
		assertEquals(expectedDto, result);
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).findNotifByNotifType(notification);
	}


	@Test
	void Given_NotificationAndRequest_When_AddNotif_Then_InvokeServiceOnce() {
		// given
		NotifType notifType = NotifType.TIME;
		List<Integer> dayOfWeekList = List.of(1, 2, 3);

		when(notification.getNotifType()).thenReturn(notifType);
		when(timeNotificationService.supports(notifType)).thenReturn(true);

		// when
		selector.addNotif(notification, dayOfWeekList, request);

		// then
		verify(timeNotificationService).supports(notifType);
		verify(timeNotificationService).addNotifDetail(notification, dayOfWeekList, request);
	}


	@Test
	void Given_UnsupportedNotificationType_When_FindNotifByNotifType_Then_ThrowServerException() {
		// given
		NotifType unsupportedType = NotifType.TIME;

		when(notification.getNotifType()).thenReturn(unsupportedType);
		when(timeNotificationService.supports(unsupportedType)).thenReturn(false);
		when(anotherService.supports(unsupportedType)).thenReturn(false);

		// when & then
		ServerException exception = assertThrows(ServerException.class, () ->
			selector.findNotifByNotifType(notification)
		);

		assertEquals(NotificationErrorResult.UNSUPPORTED_NOTIF, exception.getErrorResult());
	}

}
