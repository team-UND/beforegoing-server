package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;
import com.und.server.notification.exception.NotificationErrorResult;
import com.und.server.notification.repository.TimeNotificationRepository;

@ExtendWith(MockitoExtension.class)
class TimeNotificationServiceTest {

	@InjectMocks
	private TimeNotificationService timeNotificationService;

	@Mock
	private TimeNotificationRepository timeNotifRepository;


	@Test
	void Given_ActiveNotificationWithSingleNullDay_When_FindNotifByNotifType_Then_ReturnEverydayTrue() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		TimeNotification base = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(null)
			.hour(9)
			.minute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(base));

		// when
		NotificationInfoDto result = timeNotificationService.findNotifByNotifType(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEveryDay()).isTrue();
		assertThat(result.dayOfWeekOrdinalList()).isEmpty();
		assertThat(result.notificationCondition()).isInstanceOf(TimeNotificationResponse.class);
	}


	@Test
	void Given_ActiveNotificationWithMultipleDays_When_FindNotifByNotifType_Then_ReturnDayList() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		TimeNotification monday = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(DayOfWeek.MONDAY)
			.hour(10)
			.minute(30)
			.build();

		TimeNotification wednesday = TimeNotification.builder()
			.id(11L)
			.dayOfWeek(DayOfWeek.WEDNESDAY)
			.hour(10)
			.minute(30)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(monday, wednesday));

		// when
		NotificationInfoDto result = timeNotificationService.findNotifByNotifType(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEveryDay()).isFalse();
		assertThat(result.dayOfWeekOrdinalList()).hasSize(2);
		assertThat(result.dayOfWeekOrdinalList().get(0).dayOfWeekOrdinal()).isEqualTo(DayOfWeek.MONDAY.ordinal());
		assertThat(result.notificationCondition()).isInstanceOf(TimeNotificationResponse.class);
	}


	@Test
	void Given_ActiveNotificationWithEmptyTimeList_When_FindNotifByNotifType_Then_ThrowException() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of());

		// when & then
		assertThatThrownBy(() ->
			timeNotificationService.findNotifByNotifType(notification)
		).isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(NotificationErrorResult.NOT_FOUND_NOTIF);
	}


	@Test
	void Given_InactiveNotification_When_FindNotifByNotifType_Then_ReturnNull() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notifType(NotifType.TIME)
			.build();

		// when
		NotificationInfoDto result = timeNotificationService.findNotifByNotifType(notification);

		// then
		assertThat(result).isNull();
	}


	@Test
	void Given_7DaysAndActiveNotification_When_AddNotifDetail_Then_SaveOnce() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		TimeNotificationRequest request = new TimeNotificationRequest(8, 30);

		List<Integer> allDays = List.of(0, 1, 2, 3, 4, 5, 6);

		// when
		timeNotificationService.addNotifDetail(notification, allDays, request);

		// then
		verify(timeNotifRepository).save(any(TimeNotification.class));
	}


	@Test
	void Given_SomeDaysAndActiveNotification_When_AddNotifDetail_Then_SaveAll() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		TimeNotificationRequest request = new TimeNotificationRequest(7, 45);

		List<Integer> days = List.of(1, 3, 5);

		// when
		timeNotificationService.addNotifDetail(notification, days, request);

		// then
		verify(timeNotifRepository).saveAll(anyList());
	}


	@Test
	void Given_InactiveNotification_When_AddNotifDetail_Then_DoNothing() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notifType(NotifType.TIME)
			.build();

		TimeNotificationRequest request = new TimeNotificationRequest(7, 0);
		List<Integer> days = List.of(1, 2, 3);

		// when
		timeNotificationService.addNotifDetail(notification, days, request);

		// then
		verifyNoInteractions(timeNotifRepository);
	}

}
