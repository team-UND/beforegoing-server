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

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
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
	void Given_ActiveNotification_When_FindNotificationDetails_Then_ReturnNotificationInfoDto() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		NotificationConditionResponse expectedInfo = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();
		when(notificationConditionSelector.findNotificationCondition(notification))
			.thenReturn(expectedInfo);

		// when
		NotificationConditionResponse result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isEqualTo(expectedInfo);
		verify(notificationConditionSelector).findNotificationCondition(notification);
	}


	@Test
	void Given_NotificationRequestAndCondition_When_AddNotification_Then_SaveNotificationAndAddCondition() {
		// given
		NotificationRequest notificationInfo = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(0, 1, 2))
			.build();

		TimeNotificationRequest conditionInfo = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		Notification savedNotification = Notification.builder()
			.id(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.isActive(true)
			.build();

		when(notificationRepository.save(any(Notification.class)))
			.thenReturn(savedNotification);

		// when
		Notification result = notificationService.addNotification(notificationInfo, conditionInfo);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getNotificationType()).isEqualTo(NotificationType.TIME);
		assertThat(result.getNotificationMethodType()).isEqualTo(NotificationMethodType.PUSH);
		assertThat(result.getIsActive()).isTrue();
		verify(notificationRepository).save(any(Notification.class));
		verify(notificationConditionSelector)
			.addNotificationCondition(any(Notification.class), eq(conditionInfo));
	}


	@Test
	void Given_ActiveNotificationAndSameType_When_UpdateNotification_Then_UpdateNotificationAndCondition() {
		// given
		Notification oldNotification = Notification.builder()
			.id(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.isActive(true)
			.build();

		NotificationRequest notificationInfo = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(0, 1, 2, 3))
			.build();

		TimeNotificationRequest conditionInfo = TimeNotificationRequest.builder()
			.startHour(10)
			.startMinute(30)
			.build();

		// when
		notificationService.updateNotification(oldNotification, notificationInfo, conditionInfo);

		// then
		assertThat(oldNotification.getNotificationType()).isEqualTo(NotificationType.TIME);
		assertThat(oldNotification.getNotificationMethodType()).isEqualTo(NotificationMethodType.ALARM);
		assertThat(oldNotification.isActive()).isTrue();
		verify(notificationConditionSelector)
			.updateNotificationCondition(oldNotification, conditionInfo);
	}


	@Test
	void Given_ActiveNotificationAndDifferentType_When_UpdateNotification_Then_DeleteOldAndAddNew() {
		// given
		Notification oldNotification = Notification.builder()
			.id(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.isActive(true)
			.build();

		NotificationRequest notificationInfo = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.LOCATION)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(0, 1, 2))
			.build();

		TimeNotificationRequest conditionInfo = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		// when
		notificationService.updateNotification(oldNotification, notificationInfo, conditionInfo);

		// then
		assertThat(oldNotification.getNotificationType()).isEqualTo(NotificationType.LOCATION);
		assertThat(oldNotification.getNotificationMethodType()).isEqualTo(NotificationMethodType.ALARM);
		assertThat(oldNotification.isActive()).isTrue();
		verify(notificationConditionSelector).deleteNotificationCondition(
			NotificationType.TIME, oldNotification.getId());
		verify(notificationConditionSelector)
			.addNotificationCondition(oldNotification, conditionInfo);
	}


	@Test
	void Given_ActiveNotificationAndInactive_When_UpdateNotification_Then_DeleteCondition() {
		// given
		Notification oldNotification = Notification.builder()
			.id(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.isActive(true)
			.build();

		NotificationRequest notificationRequest = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		// when
		notificationService.updateNotification(oldNotification, notificationRequest, null);

		// then
		assertThat(oldNotification.isActive()).isFalse();
		assertThat(oldNotification.getNotificationMethodType()).isNull();
		verify(notificationConditionSelector)
			.deleteNotificationCondition(NotificationType.TIME, oldNotification.getId());
	}

	@Test
	void Given_NotificationType_When_AddWithoutNotification_Then_CreateInactiveNotification() {
		// given
		NotificationType type = NotificationType.TIME;
		Notification saved = Notification.builder()
			.id(10L)
			.notificationType(type)
			.isActive(false)
			.build();

		when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

		// when
		NotificationRequest request = NotificationRequest.builder()
			.isActive(false)
			.notificationType(type)
			.build();

		Notification result = notificationService.addNotification(request, null);

		// then
		assertThat(result.getNotificationType()).isEqualTo(type);
		assertThat(result.getIsActive()).isFalse();
		verify(notificationRepository).save(any(Notification.class));
	}

	@Test
	void Given_Notification_When_DeleteNotification_Then_DeletesCondition() {
		// given
		Notification notification = Notification.builder()
			.id(5L)
			.notificationType(NotificationType.LOCATION)
			.isActive(true)
			.build();

		// when
		notificationService.deleteNotification(notification);

		// then
		verify(notificationConditionSelector)
			.deleteNotificationCondition(NotificationType.LOCATION, 5L);
	}


	@Test
	void Given_NotificationWithDaysOfWeek_When_FindNotificationDetails_Then_ReturnNotificationWithDays() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,1,2,3,4,5,6")
			.build();

		NotificationConditionResponse expectedInfo = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();
		when(notificationConditionSelector.findNotificationCondition(notification))
			.thenReturn(expectedInfo);

		// when
		NotificationConditionResponse result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isEqualTo(expectedInfo);
		assertThat(notification.isEveryDay()).isTrue();
		assertThat(notification.getDaysOfWeekOrdinalList()).hasSize(7);
		assertThat(notification.getDaysOfWeekOrdinalList()).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6);
		verify(notificationConditionSelector).findNotificationCondition(notification);
	}


	@Test
	void Given_NotificationWithSpecificDays_When_FindNotificationDetails_Then_ReturnNotificationWithSpecificDays() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,2,4")
			.build();

		NotificationConditionResponse expectedInfo = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();
		when(notificationConditionSelector.findNotificationCondition(notification))
			.thenReturn(expectedInfo);

		// when
		NotificationConditionResponse result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isEqualTo(expectedInfo);
		assertThat(notification.isEveryDay()).isFalse();
		assertThat(notification.getDaysOfWeekOrdinalList()).hasSize(3);
		assertThat(notification.getDaysOfWeekOrdinalList()).containsExactlyInAnyOrder(0, 2, 4);
		verify(notificationConditionSelector).findNotificationCondition(notification);
	}


	@Test
	void Given_NotificationWithEmptyDays_When_FindNotificationDetails_Then_ReturnNotificationWithEmptyDays() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("")
			.build();

		NotificationConditionResponse expectedInfo = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();
		when(notificationConditionSelector.findNotificationCondition(notification))
			.thenReturn(expectedInfo);

		// when
		NotificationConditionResponse result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isEqualTo(expectedInfo);
		assertThat(notification.isEveryDay()).isFalse();
		assertThat(notification.getDaysOfWeekOrdinalList()).isEmpty();
		verify(notificationConditionSelector).findNotificationCondition(notification);
	}


	@Test
	void Given_NotificationWithNullDays_When_FindNotificationDetails_Then_ReturnNotificationWithEmptyDays() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek(null)
			.build();

		NotificationConditionResponse expectedInfo = TimeNotificationResponse.builder()
			.startHour(9)
			.startMinute(0)
			.build();
		when(notificationConditionSelector.findNotificationCondition(notification))
			.thenReturn(expectedInfo);

		// when
		NotificationConditionResponse result = notificationService.findNotificationDetails(notification);

		// then
		assertThat(result).isEqualTo(expectedInfo);
		assertThat(notification.isEveryDay()).isFalse();
		assertThat(notification.getDaysOfWeekOrdinalList()).isEmpty();
		verify(notificationConditionSelector).findNotificationCondition(notification);
	}


	@Test
	void Given_ActiveNotification_When_UpdateDaysOfWeekOrdinal_Then_UpdateSuccessfully() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,1,2")
			.build();

		List<Integer> newDays = List.of(1, 3, 5);

		// when
		notification.updateDaysOfWeekOrdinal(newDays);

		// then
		assertThat(notification.getDaysOfWeekOrdinalList()).hasSize(3);
		assertThat(notification.getDaysOfWeekOrdinalList()).containsExactlyInAnyOrder(1, 3, 5);
		assertThat(notification.isEveryDay()).isFalse();
	}


	@Test
	void Given_InactiveNotification_When_UpdateDaysOfWeekOrdinal_Then_SetToNull() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,1,2")
			.build();

		List<Integer> newDays = List.of(1, 3, 5);

		// when
		notification.updateDaysOfWeekOrdinal(newDays);

		// then
		assertThat(notification.getDaysOfWeekOrdinalList()).isEmpty();
		assertThat(notification.isEveryDay()).isFalse();
	}


	@Test
	void Given_ActiveNotification_When_UpdateDaysOfWeekOrdinalWithNull_Then_SetToNull() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,1,2")
			.build();

		// when
		notification.updateDaysOfWeekOrdinal(null);

		// then
		assertThat(notification.getDaysOfWeekOrdinalList()).isEmpty();
		assertThat(notification.isEveryDay()).isFalse();
	}


	@Test
	void Given_ActiveNotification_When_UpdateDaysOfWeekOrdinalWithEmpty_Then_SetToNull() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,1,2")
			.build();

		// when
		notification.updateDaysOfWeekOrdinal(List.of());

		// then
		assertThat(notification.getDaysOfWeekOrdinalList()).isEmpty();
		assertThat(notification.isEveryDay()).isFalse();
	}


	@Test
	void Given_Notification_When_UpdateDaysOfWeekOrdinalWithEmptyList_Then_SetToNull() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.daysOfWeek("0,1,2")
			.build();

		// when
		notification.updateDaysOfWeekOrdinal(List.of());

		// then
		assertThat(notification.getDaysOfWeekOrdinalList()).isEmpty();
		assertThat(notification.isEveryDay()).isFalse();
	}


	@Test
	void Given_Notification_When_DeactivateNotification_Then_SetMethodTypeToNull() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.build();

		// when
		notification.deactivate();

		// then
		assertThat(notification.getNotificationMethodType()).isNull();
		assertThat(notification.isActive()).isFalse();
	}

}
