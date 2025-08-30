package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.scenario.repository.ScenarioRepository;


@ExtendWith(MockitoExtension.class)
class ScenarioNotificationServiceTest {

	@InjectMocks
	private ScenarioNotificationService scenarioNotificationService;

	@Mock
	private ScenarioRepository scenarioRepository;

	private final Long memberId = 1L;


	@Test
	void Given_ValidMemberId_When_GetScenarioNotifications_Then_ReturnTimeNotifications() {
		// given
		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		ScenarioNotificationResponse expectedResponse = ScenarioNotificationResponse.builder()
			.scenarioId(1L)
			.scenarioName("아침 루틴")
			.memo("아침에 할 일들")
			.notificationId(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(1, 2, 3, 4, 5))
			.notificationCondition(timeNotificationResponse)
			.build();

		given(scenarioRepository.findTimeScenarioNotifications(memberId))
			.willReturn(List.of(expectedResponse));

		// when
		List<ScenarioNotificationResponse> result = scenarioNotificationService.getScenarioNotifications(memberId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).scenarioId()).isEqualTo(1L);
		assertThat(result.get(0).scenarioName()).isEqualTo("아침 루틴");
		assertThat(result.get(0).notificationType()).isEqualTo(NotificationType.TIME);
		assertThat(result.get(0).notificationCondition()).isEqualTo(timeNotificationResponse);
	}


	@Test
	void Given_ValidMemberId_When_GetScenarioNotifications_Then_ReturnMultipleTimeNotifications() {
		// given
		TimeNotificationResponse morningNotification = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		TimeNotificationResponse eveningNotification = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(18)
			.startMinute(0)
			.build();

		ScenarioNotificationResponse morningResponse = ScenarioNotificationResponse.builder()
			.scenarioId(1L)
			.scenarioName("아침 루틴")
			.memo("아침에 할 일들")
			.notificationId(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(1, 2, 3, 4, 5))
			.notificationCondition(morningNotification)
			.build();

		ScenarioNotificationResponse eveningResponse = ScenarioNotificationResponse.builder()
			.scenarioId(2L)
			.scenarioName("저녁 루틴")
			.memo("저녁에 할 일들")
			.notificationId(2L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(1, 2, 3, 4, 5, 6, 7))
			.notificationCondition(eveningNotification)
			.build();

		given(scenarioRepository.findTimeScenarioNotifications(memberId))
			.willReturn(List.of(morningResponse, eveningResponse));

		// when
		List<ScenarioNotificationResponse> result = scenarioNotificationService.getScenarioNotifications(memberId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).scenarioId()).isEqualTo(1L);
		assertThat(result.get(0).scenarioName()).isEqualTo("아침 루틴");
		assertThat(result.get(1).scenarioId()).isEqualTo(2L);
		assertThat(result.get(1).scenarioName()).isEqualTo("저녁 루틴");
	}


	@Test
	void Given_ValidMemberId_When_GetScenarioNotifications_Then_ReturnEmptyList() {
		// given
		given(scenarioRepository.findTimeScenarioNotifications(memberId))
			.willReturn(List.of());

		// when
		List<ScenarioNotificationResponse> result = scenarioNotificationService.getScenarioNotifications(memberId);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	void Given_ValidMemberId_When_GetScenarioNotifications_Then_ReturnNotificationsWithNullDaysOfWeek() {
		// given
		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(12)
			.startMinute(0)
			.build();

		ScenarioNotificationResponse expectedResponse = ScenarioNotificationResponse.builder()
			.scenarioId(1L)
			.scenarioName("점심 루틴")
			.memo("점심에 할 일들")
			.notificationId(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of())
			.notificationCondition(timeNotificationResponse)
			.build();

		given(scenarioRepository.findTimeScenarioNotifications(memberId))
			.willReturn(List.of(expectedResponse));

		// when
		List<ScenarioNotificationResponse> result = scenarioNotificationService.getScenarioNotifications(memberId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).daysOfWeekOrdinal()).isEmpty();
	}


	@Test
	void Given_ValidMemberId_When_GetScenarioNotifications_Then_ReturnNotificationsWithDifferentNotificationMethods() {
		// given
		TimeNotificationResponse timeNotificationResponse = TimeNotificationResponse.builder()
			.notificationType(NotificationType.TIME)
			.startHour(10)
			.startMinute(0)
			.build();

		ScenarioNotificationResponse pushResponse = ScenarioNotificationResponse.builder()
			.scenarioId(1L)
			.scenarioName("푸시 알림 루틴")
			.memo("푸시 알림으로 받는 루틴")
			.notificationId(1L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(1, 2, 3, 4, 5))
			.notificationCondition(timeNotificationResponse)
			.build();

		ScenarioNotificationResponse alarmResponse = ScenarioNotificationResponse.builder()
			.scenarioId(2L)
			.scenarioName("알람 루틴")
			.memo("알람으로 받는 루틴")
			.notificationId(2L)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(1, 2, 3, 4, 5))
			.notificationCondition(timeNotificationResponse)
			.build();

		given(scenarioRepository.findTimeScenarioNotifications(memberId))
			.willReturn(List.of(pushResponse, alarmResponse));

		// when
		List<ScenarioNotificationResponse> result = scenarioNotificationService.getScenarioNotifications(memberId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).notificationMethodType()).isEqualTo(NotificationMethodType.PUSH);
		assertThat(result.get(1).notificationMethodType()).isEqualTo(NotificationMethodType.ALARM);
	}

}
