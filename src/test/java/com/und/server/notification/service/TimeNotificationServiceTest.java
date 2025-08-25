package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;
import com.und.server.notification.repository.TimeNotificationRepository;

@ExtendWith(MockitoExtension.class)
class TimeNotificationServiceTest {

	@Mock
	private TimeNotificationRepository timeNotifRepository;

	@InjectMocks
	private TimeNotificationService timeNotificationService;


	@Test
	void Given_TimeNotifType_When_Supports_Then_ReturnTrue() {
		// given
		NotificationType timeType = NotificationType.TIME;

		// when
		boolean result = timeNotificationService.supports(timeType);

		// then
		assertThat(result).isTrue();
	}


	@Test
	void Given_LocationNotifType_When_Supports_Then_ReturnFalse() {
		// given
		NotificationType locationType = NotificationType.LOCATION;

		// when
		boolean result = timeNotificationService.supports(locationType);

		// then
		assertThat(result).isFalse();
	}


	@Test
	void Given_EverydayNotification_When_FindNotificationInfoByType_Then_ReturnEverydayTrue() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,1,2,3,4,5,6")
			.build();

		TimeNotification timeNotification = TimeNotification.builder()
			.id(10L)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(timeNotification);

		// when
		NotificationConditionResponse result = timeNotificationService.findNotificationInfoByType(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(TimeNotificationResponse.class);
		TimeNotificationResponse timeResponse = (TimeNotificationResponse) result;
		assertThat(timeResponse.startHour()).isEqualTo(9);
		assertThat(timeResponse.startMinute()).isEqualTo(0);
	}


	@Test
	void Given_SpecificDaysNotification_When_FindNotificationInfoByType_Then_ReturnDayList() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,2")
			.build();

		TimeNotification timeNotification = TimeNotification.builder()
			.id(10L)
			.startHour(10)
			.startMinute(30)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(timeNotification);

		// when
		NotificationConditionResponse result = timeNotificationService.findNotificationInfoByType(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(TimeNotificationResponse.class);
		TimeNotificationResponse timeResponse = (TimeNotificationResponse) result;
		assertThat(timeResponse.startHour()).isEqualTo(10);
		assertThat(timeResponse.startMinute()).isEqualTo(30);
	}


	@Test
	void Given_InactiveNotification_When_FindNotificationInfoByType_Then_ReturnNull() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		// when
		NotificationConditionResponse result = timeNotificationService.findNotificationInfoByType(notification);

		// then
		assertThat(result).isNull();
	}


	@Test
	void Given_ActiveNotification_When_AddNotificationCondition_Then_SaveTimeNotification() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		TimeNotificationRequest request = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		TimeNotification savedTimeNotification = TimeNotification.builder()
			.id(1L)
			.notification(notification)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.save(any(TimeNotification.class)))
			.thenReturn(savedTimeNotification);

		// when
		timeNotificationService.addNotificationCondition(notification, request);

		// then
		verify(timeNotifRepository).save(any(TimeNotification.class));
	}


	@Test
	void Given_InactiveNotification_When_AddNotificationCondition_Then_DoNothing() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		TimeNotificationRequest request = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		// when
		timeNotificationService.addNotificationCondition(notification, request);

		// then
		// verify no interaction with repository
	}


	@Test
	void Given_ExistingTimeNotification_When_UpdateNotificationCondition_Then_UpdateTimeCondition() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		TimeNotificationRequest request = TimeNotificationRequest.builder()
			.startHour(10)
			.startMinute(30)
			.build();

		TimeNotification existingTimeNotification = TimeNotification.builder()
			.id(1L)
			.notification(notification)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(existingTimeNotification);

		// when
		timeNotificationService.updateNotificationCondition(notification, request);

		// then
		assertThat(existingTimeNotification.getStartHour()).isEqualTo(10);
		assertThat(existingTimeNotification.getStartMinute()).isEqualTo(30);
	}


	@Test
	void Given_NoExistingTimeNotification_When_UpdateNotificationCondition_Then_AddNewTimeNotification() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		TimeNotificationRequest request = TimeNotificationRequest.builder()
			.startHour(10)
			.startMinute(30)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(null);

		// when
		timeNotificationService.updateNotificationCondition(notification, request);

		// then
		verify(timeNotifRepository).save(any(TimeNotification.class));
	}


	@Test
	void Given_NotificationId_When_DeleteNotificationCondition_Then_DeleteByNotificationId() {
		// given
		Long notificationId = 1L;

		// when
		timeNotificationService.deleteNotificationCondition(notificationId);

		// then
		verify(timeNotifRepository).deleteByNotificationId(notificationId);
	}

}
