package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.und.server.notification.event.NotificationEventPublisher;
import com.und.server.notification.repository.NotificationRepository;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.repository.ScenarioRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationConditionSelector notificationConditionSelector;

	@Mock
	private ScenarioRepository scenarioRepository;

	@Mock
	private NotificationEventPublisher notificationEventPublisher;

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


	@Test
	void Given_MemberWithActiveNotis_When_UpdateNotiActiveStatusToFalse_Then_DeactivateNotisAndPublishEvent() {
		// given
		Long memberId = 1L;
		Boolean isActive = false;

		Notification notification1 = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeek("0,1,2")
			.build();

		Notification notification2 = Notification.builder()
			.id(2L)
			.isActive(true)
			.notificationType(NotificationType.LOCATION)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeek("0,1,2,3,4")
			.build();

		Scenario scenario1 = Scenario.builder()
			.id(1L)
			.notification(notification1)
			.build();

		Scenario scenario2 = Scenario.builder()
			.id(2L)
			.notification(notification2)
			.build();

		when(scenarioRepository.findByMemberId(memberId))
			.thenReturn(List.of(scenario1, scenario2));

		// when
		notificationService.updateNotificationActiveStatus(memberId, isActive);

		// then
		assertThat(notification1.isActive()).isFalse();
		assertThat(notification2.isActive()).isFalse();
		verify(notificationEventPublisher).publishActiveUpdateEvent(memberId, isActive);
	}


	@Test
	void Given_MemberWithInactiveNotis_When_UpdateNotiActiveStatusToTrue_Then_ActivateNotisAndPublishEvent() {
		// given
		Long memberId = 1L;
		Boolean isActive = true;

		Notification notification1 = Notification.builder()
			.id(1L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeek("0,1,2")
			.build();

		Notification notification2 = Notification.builder()
			.id(2L)
			.isActive(false)
			.notificationType(NotificationType.LOCATION)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeek("0,1,2,3,4")
			.build();

		Scenario scenario1 = Scenario.builder()
			.id(1L)
			.notification(notification1)
			.build();

		Scenario scenario2 = Scenario.builder()
			.id(2L)
			.notification(notification2)
			.build();

		when(scenarioRepository.findByMemberId(memberId))
			.thenReturn(List.of(scenario1, scenario2));

		// when
		notificationService.updateNotificationActiveStatus(memberId, isActive);

		// then
		assertThat(notification1.isActive()).isTrue();
		assertThat(notification2.isActive()).isTrue();
		verify(notificationEventPublisher).publishActiveUpdateEvent(memberId, isActive);
	}


	@Test
	void Given_MemberWithMixedNotis_When_UpdateNotiActiveStatusToFalse_Then_OnlyDeactivateActiveNotis() {
		// given
		Long memberId = 1L;
		Boolean isActive = false;

		Notification activeNotification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeek("0,1,2")
			.build();

		Notification inactiveNotification = Notification.builder()
			.id(2L)
			.isActive(false)
			.notificationType(NotificationType.LOCATION)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeek("0,1,2,3,4")
			.build();

		Scenario scenario1 = Scenario.builder()
			.id(1L)
			.notification(activeNotification)
			.build();

		Scenario scenario2 = Scenario.builder()
			.id(2L)
			.notification(inactiveNotification)
			.build();

		when(scenarioRepository.findByMemberId(memberId))
			.thenReturn(List.of(scenario1, scenario2));

		// when
		notificationService.updateNotificationActiveStatus(memberId, isActive);

		// then
		assertThat(activeNotification.isActive()).isFalse();
		assertThat(inactiveNotification.isActive()).isFalse(); // Should remain false
		verify(notificationEventPublisher).publishActiveUpdateEvent(memberId, isActive);
	}


	@Test
	void Given_MemberWithNoScenarios_When_UpdateNotificationActiveStatus_Then_DoNothing() {
		// given
		Long memberId = 1L;
		Boolean isActive = true;

		when(scenarioRepository.findByMemberId(memberId))
			.thenReturn(List.of());

		// when
		notificationService.updateNotificationActiveStatus(memberId, isActive);

		// then
		verify(notificationEventPublisher, never()).publishActiveUpdateEvent(anyLong(), anyBoolean());
	}


	@Test
	void Given_MemberWithScenariosButNoNotifications_When_UpdateNotificationActiveStatus_Then_PublishEvent() {
		// given
		Long memberId = 1L;
		Boolean isActive = true;

		Scenario scenario1 = Scenario.builder()
			.id(1L)
			.notification(null)
			.build();

		Scenario scenario2 = Scenario.builder()
			.id(2L)
			.notification(null)
			.build();

		when(scenarioRepository.findByMemberId(memberId))
			.thenReturn(List.of(scenario1, scenario2));

		// when
		notificationService.updateNotificationActiveStatus(memberId, isActive);

		// then
		verify(notificationEventPublisher).publishActiveUpdateEvent(memberId, isActive);
	}


	@Test
	void Given_MemberWithNotisWithoutConditions_When_UpdateNotiActiveStatusToTrue_Then_ActivateNotis() {
		// given
		Long memberId = 1L;
		Boolean isActive = true;

		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeek("0,1,2")
			.build();

		Scenario scenario = Scenario.builder()
			.id(1L)
			.notification(notification)
			.build();

		when(scenarioRepository.findByMemberId(memberId))
			.thenReturn(List.of(scenario));

		// when
		notificationService.updateNotificationActiveStatus(memberId, isActive);

		// then
		assertThat(notification.isActive()).isTrue();
		verify(notificationEventPublisher).publishActiveUpdateEvent(memberId, isActive);
	}

}
