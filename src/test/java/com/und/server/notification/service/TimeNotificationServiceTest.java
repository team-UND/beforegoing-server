package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.TimeNotificationRequest;
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
			.build();

		TimeNotification monday = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(DayOfWeek.MONDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification tuesday = TimeNotification.builder()
			.id(11L)
			.dayOfWeek(DayOfWeek.TUESDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification wednesday = TimeNotification.builder()
			.id(12L)
			.dayOfWeek(DayOfWeek.WEDNESDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification thursday = TimeNotification.builder()
			.id(13L)
			.dayOfWeek(DayOfWeek.THURSDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification friday = TimeNotification.builder()
			.id(14L)
			.dayOfWeek(DayOfWeek.FRIDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification saturday = TimeNotification.builder()
			.id(15L)
			.dayOfWeek(DayOfWeek.SATURDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification sunday = TimeNotification.builder()
			.id(16L)
			.dayOfWeek(DayOfWeek.SUNDAY)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(monday, tuesday, wednesday, thursday, friday, saturday, sunday));

		// when
		NotificationInfoDto result = timeNotificationService.findNotificationInfoByType(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEveryDay()).isTrue();
		assertThat(result.daysOfWeekOrdinal()).hasSize(7);
		assertThat(result.daysOfWeekOrdinal()).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6);
		assertThat(result.notificationConditionResponse()).isInstanceOf(TimeNotificationResponse.class);
	}


	@Test
	void Given_SpecificDaysNotification_When_FindNotificationInfoByType_Then_ReturnDayList() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		TimeNotification monday = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(DayOfWeek.MONDAY)
			.startHour(10)
			.startMinute(30)
			.build();

		TimeNotification wednesday = TimeNotification.builder()
			.id(11L)
			.dayOfWeek(DayOfWeek.WEDNESDAY)
			.startHour(10)
			.startMinute(30)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(monday, wednesday));

		// when
		NotificationInfoDto result = timeNotificationService.findNotificationInfoByType(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEveryDay()).isFalse();
		assertThat(result.daysOfWeekOrdinal()).hasSize(2);
		assertThat(result.daysOfWeekOrdinal()).containsExactlyInAnyOrder(0, 2);
		assertThat(result.notificationConditionResponse()).isInstanceOf(TimeNotificationResponse.class);
	}


	@Test
	void Given_7DaysAndActiveNotification_When_AddNotificationCondition_Then_SaveAll7Days() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		TimeNotificationRequest request = TimeNotificationRequest.builder()
			.startHour(8)
			.startMinute(30)
			.build();
		List<Integer> allDays = List.of(0, 1, 2, 3, 4, 5, 6);

		// when
		timeNotificationService.addNotificationCondition(notification, allDays, request);

		// then
		verify(timeNotifRepository).saveAll(anyList());
		verify(timeNotifRepository).saveAll(argThat(list -> {
			assertThat(list).hasSize(7);
			Set<DayOfWeek> savedDays = ((List<TimeNotification>) list).stream()
				.map(TimeNotification::getDayOfWeek)
				.collect(Collectors.toSet());
			assertThat(savedDays).containsExactlyInAnyOrder(
				DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
				DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
			);
			return true;
		}));
	}


	@Test
	void Given_SomeDaysAndActiveNotification_When_AddNotificationCondition_Then_SaveAll() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		TimeNotificationRequest request = TimeNotificationRequest.builder()
			.startHour(7)
			.startMinute(45)
			.build();
		List<Integer> days = List.of(1, 3, 5); // TUESDAY, THURSDAY, SATURDAY

		// when
		timeNotificationService.addNotificationCondition(notification, days, request);

		// then
		verify(timeNotifRepository).saveAll(anyList());
	}


	@Test
	void Given_NotificationId_When_DeleteNotificationCondition_Then_DeleteAll() {
		// given
		Long notificationId = 1L;
		List<TimeNotification> timeNotifications = List.of(
			TimeNotification.builder().id(1L).build(),
			TimeNotification.builder().id(2L).build()
		);
		when(timeNotifRepository.findByNotificationId(notificationId))
			.thenReturn(timeNotifications);

		// when
		timeNotificationService.deleteNotificationCondition(notificationId);

		// then
		verify(timeNotifRepository).findByNotificationId(notificationId);
		verify(timeNotifRepository).deleteAll(timeNotifications);
	}


	@Test
	void Given_EverydayNotificationAnd7Days_When_UpdateNotificationCondition_Then_UpdateAll7Days() {
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
		List<Integer> allDays = List.of(0, 1, 2, 3, 4, 5, 6);

		TimeNotification existingMonday = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(DayOfWeek.MONDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingTuesday = TimeNotification.builder()
			.id(11L)
			.dayOfWeek(DayOfWeek.TUESDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingWednesday = TimeNotification.builder()
			.id(12L)
			.dayOfWeek(DayOfWeek.WEDNESDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingThursday = TimeNotification.builder()
			.id(13L)
			.dayOfWeek(DayOfWeek.THURSDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingFriday = TimeNotification.builder()
			.id(14L)
			.dayOfWeek(DayOfWeek.FRIDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingSaturday = TimeNotification.builder()
			.id(15L)
			.dayOfWeek(DayOfWeek.SATURDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingSunday = TimeNotification.builder()
			.id(16L)
			.dayOfWeek(DayOfWeek.SUNDAY)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(existingMonday, existingTuesday, existingWednesday,
				existingThursday, existingFriday, existingSaturday, existingSunday));

		// when
		timeNotificationService.updateNotificationCondition(notification, allDays, request);

		// then
		verify(timeNotifRepository).findByNotificationId(notification.getId());
		verify(timeNotifRepository).saveAll(anyList());
		assertThat(existingMonday.getStartHour()).isEqualTo(10);
		assertThat(existingMonday.getStartMinute()).isEqualTo(30);
		assertThat(existingTuesday.getStartHour()).isEqualTo(10);
		assertThat(existingTuesday.getStartMinute()).isEqualTo(30);
		assertThat(existingWednesday.getStartHour()).isEqualTo(10);
		assertThat(existingWednesday.getStartMinute()).isEqualTo(30);
		assertThat(existingThursday.getStartHour()).isEqualTo(10);
		assertThat(existingThursday.getStartMinute()).isEqualTo(30);
		assertThat(existingFriday.getStartHour()).isEqualTo(10);
		assertThat(existingFriday.getStartMinute()).isEqualTo(30);
		assertThat(existingSaturday.getStartHour()).isEqualTo(10);
		assertThat(existingSaturday.getStartMinute()).isEqualTo(30);
		assertThat(existingSunday.getStartHour()).isEqualTo(10);
		assertThat(existingSunday.getStartMinute()).isEqualTo(30);
	}


	@Test
	void Given_SpecificDaysNotificationAndSameDays_When_UpdateNotificationCondition_Then_UpdateExistingDays() {
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
		List<Integer> sameDays = List.of(0, 2); // MONDAY, WEDNESDAY

		TimeNotification existingMonday = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(DayOfWeek.MONDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingWednesday = TimeNotification.builder()
			.id(11L)
			.dayOfWeek(DayOfWeek.WEDNESDAY)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(existingMonday, existingWednesday));

		// when
		timeNotificationService.updateNotificationCondition(notification, sameDays, request);

		// then
		verify(timeNotifRepository).findByNotificationId(notification.getId());
		verify(timeNotifRepository).saveAll(anyList());
		assertThat(existingMonday.getStartHour()).isEqualTo(10);
		assertThat(existingMonday.getStartMinute()).isEqualTo(30);
		assertThat(existingWednesday.getStartHour()).isEqualTo(10);
		assertThat(existingWednesday.getStartMinute()).isEqualTo(30);
	}


	@Test
	void Given_SpecificDaysNotificationAndDifferentDays_When_UpdateNotificationCondition_Then_DeleteOldAndAddNew() {
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
		List<Integer> newDays = List.of(1, 3); // TUESDAY, THURSDAY

		TimeNotification existingMonday = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(DayOfWeek.MONDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingWednesday = TimeNotification.builder()
			.id(11L)
			.dayOfWeek(DayOfWeek.WEDNESDAY)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(existingMonday, existingWednesday));

		// when
		timeNotificationService.updateNotificationCondition(notification, newDays, request);

		// then
		verify(timeNotifRepository).findByNotificationId(notification.getId());
		verify(timeNotifRepository).deleteAll(anyList());
		verify(timeNotifRepository).saveAll(anyList());
	}


	@Test
	void Given_SpecificDaysNotificationAndMixedDays_When_UpdateNotificationCondition_Then_DeleteAddAndUpdate() {
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
		List<Integer> mixedDays = List.of(0, 1, 3); // MONDAY, TUESDAY, THURSDAY

		TimeNotification existingMonday = TimeNotification.builder()
			.id(10L)
			.dayOfWeek(DayOfWeek.MONDAY)
			.startHour(9)
			.startMinute(0)
			.build();
		TimeNotification existingWednesday = TimeNotification.builder()
			.id(11L)
			.dayOfWeek(DayOfWeek.WEDNESDAY)
			.startHour(9)
			.startMinute(0)
			.build();

		when(timeNotifRepository.findByNotificationId(notification.getId()))
			.thenReturn(List.of(existingMonday, existingWednesday));

		// when
		timeNotificationService.updateNotificationCondition(notification, mixedDays, request);

		// then
		verify(timeNotifRepository).findByNotificationId(notification.getId());
		verify(timeNotifRepository).deleteAll(anyList());
		verify(timeNotifRepository, org.mockito.Mockito.times(2)).saveAll(anyList());
		assertThat(existingMonday.getStartHour()).isEqualTo(10);
		assertThat(existingMonday.getStartMinute()).isEqualTo(30);
	}

}
