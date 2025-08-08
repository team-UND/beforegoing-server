package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationConditionSelector notificationConditionSelector;

	@InjectMocks
	private NotificationService notificationService;


	@Test
	void Given_Notification_When_FindNotificationDetails_Then_ReturnNotificationInfo() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		NotificationInfoDto expectedInfo = new NotificationInfoDto(true, List.of(0, 1, 2), null);
		when(notificationConditionSelector.findNotifByNotifType(notification))
			.thenReturn(expectedInfo);

		// when
		NotificationInfoDto result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isEqualTo(expectedInfo);
		verify(notificationConditionSelector).findNotifByNotifType(notification);
	}


	@Test
	void Given_NotificationRequestAndCondition_When_AddNotification_Then_SaveNotificationAndAddCondition() {
		// given
		NotificationRequest notifInfo = NotificationRequest.builder()
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.PUSH)
			.isActive(true)
			.dayOfWeekOrdinalList(List.of(0, 1, 2))
			.build();

		TimeNotificationRequest conditionInfo = new TimeNotificationRequest(9, 0);

		Notification savedNotification = Notification.builder()
			.id(1L)
			.notifType(NotifType.TIME)
			.notifMethodType(NotifMethodType.PUSH)
			.isActive(true)
			.build();

		when(notificationRepository.save(any(Notification.class)))
			.thenReturn(savedNotification);

		// when
		Notification result = notificationService.addNotification(notifInfo, conditionInfo);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getNotifType()).isEqualTo(NotifType.TIME);
		assertThat(result.getNotifMethodType()).isEqualTo(NotifMethodType.PUSH);
		assertThat(result.getIsActive()).isTrue();
		verify(notificationRepository).save(any(Notification.class));
		verify(notificationConditionSelector).addNotif(any(Notification.class), eq(List.of(0, 1, 2)),
			eq(conditionInfo));
	}


	@Test
	void Given_ActiveNotificationAndSameType_When_UpdateNotification_Then_UpdateNotificationAndCondition() {
		// given
		Notification oldNotification = Notification.builder()
			.id(1L)
			.notifType(NotifType.TIME)
			.notifMethodType(NotifMethodType.PUSH)
			.isActive(true)
			.build();

		NotificationRequest notifInfo = NotificationRequest.builder()
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.ALARM)
			.isActive(true)
			.dayOfWeekOrdinalList(List.of(0, 1, 2, 3))
			.build();

		TimeNotificationRequest conditionInfo = new TimeNotificationRequest(10, 30);

		// when
		Notification result = notificationService.updateNotification(oldNotification, notifInfo, conditionInfo);

		// then
		assertThat(result).isEqualTo(oldNotification);
		assertThat(oldNotification.getNotifType()).isEqualTo(NotifType.TIME);
		assertThat(oldNotification.getNotifMethodType()).isEqualTo(NotifMethodType.ALARM);
		assertThat(oldNotification.getIsActive()).isTrue();
		verify(notificationConditionSelector).updateNotif(oldNotification, List.of(0, 1, 2, 3), conditionInfo);
	}


	@Test
	void Given_ActiveNotificationAndDifferentType_When_UpdateNotification_Then_DeleteOldAndAddNew() {
		// given
		Notification oldNotification = Notification.builder()
			.id(1L)
			.notifType(NotifType.TIME)
			.notifMethodType(NotifMethodType.PUSH)
			.isActive(true)
			.build();

		NotificationRequest notifInfo = NotificationRequest.builder()
			.notificationType(NotifType.LOCATION)
			.notificationMethodType(NotifMethodType.ALARM)
			.isActive(true)
			.dayOfWeekOrdinalList(List.of(0, 1, 2))
			.build();

		TimeNotificationRequest conditionInfo = new TimeNotificationRequest(9, 0);

		// when
		Notification result = notificationService.updateNotification(oldNotification, notifInfo, conditionInfo);

		// then
		assertThat(result).isEqualTo(oldNotification);
		assertThat(oldNotification.getNotifType()).isEqualTo(NotifType.LOCATION);
		assertThat(oldNotification.getNotifMethodType()).isEqualTo(NotifMethodType.ALARM);
		assertThat(oldNotification.getIsActive()).isTrue();
		verify(notificationConditionSelector).deleteNotif(NotifType.TIME, oldNotification.getId());
		verify(notificationConditionSelector).addNotif(oldNotification, List.of(0, 1, 2), conditionInfo);
	}


	@Test
	void Given_ActiveNotificationAndInactive_When_UpdateNotification_Then_DeleteCondition() {
		// given
		Notification oldNotification = Notification.builder()
			.id(1L)
			.notifType(NotifType.TIME)
			.notifMethodType(NotifMethodType.PUSH)
			.isActive(true)
			.build();

		NotificationRequest notifInfo = NotificationRequest.builder()
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.PUSH)
			.isActive(false)
			.dayOfWeekOrdinalList(List.of(0, 1, 2))
			.build();

		TimeNotificationRequest conditionInfo = new TimeNotificationRequest(9, 0);

		// when
		Notification result = notificationService.updateNotification(oldNotification, notifInfo, conditionInfo);

		// then
		assertThat(result).isEqualTo(oldNotification);
		assertThat(oldNotification.getIsActive()).isFalse();
		verify(notificationConditionSelector).deleteNotif(NotifType.TIME, oldNotification.getId());
	}

}
