package com.und.server.scenario.event.publisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.event.ActiveUpdateEvent;
import com.und.server.scenario.event.MissionCheckUpdateEvent;
import com.und.server.scenario.event.MissionCreateEvent;
import com.und.server.scenario.event.MissionDeleteEvent;
import com.und.server.scenario.event.ScenarioCreateEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioOrderUpdateEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;

@ExtendWith(MockitoExtension.class)
class ScenarioEventPublisherTest {

	@InjectMocks
	private ScenarioEventPublisher scenarioEventPublisher;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private final Long memberId = 1L;
	private final Long scenarioId = 1L;

	@Test
	void Given_ValidMemberIdAndScenarioAndNotifType_When_PublishScenarioCreateEvent_Then_PublishScenarioCreateEvent() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("새로운 시나리오")
			.memo("새로운 메모")
			.notification(notification)
			.build();

		NotificationType notificationType = NotificationType.TIME;

		// when
		scenarioEventPublisher.publishScenarioCreateEvent(memberId, scenario, notificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioCreateEvent.class));
	}

	@Test
	void Given_ValidMemberIdAndScenarioIdAndDate_When_PublishMissionCreateEvent_Then_PublishMissionCreateEvent() {
		// given
		LocalDate date = LocalDate.of(2024, 1, 15);

		// when
		scenarioEventPublisher.publishMissionCreateEvent(memberId, scenarioId, date);

		// then
		verify(eventPublisher).publishEvent(any(MissionCreateEvent.class));
	}

	@Test
	void Given_ValidParametersForScenarioUpdate_When_PublishScenarioUpdateEvent_Then_PublishScenarioUpdateEvent() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("업데이트된 시나리오")
			.memo("업데이트된 메모")
			.notification(notification)
			.build();

		Boolean isOldScenarioNotificationActive = true;
		NotificationType newNotificationType = NotificationType.TIME;

		// when
		scenarioEventPublisher.publishScenarioUpdateEvent(
			memberId, scenario, isOldScenarioNotificationActive, newNotificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioUpdateEvent.class));
	}

	@Test
	void Given_ValidMemberIdAndIsActiveTrue_When_PublishNotifActiveUpdateEvent_Then_PublishActiveUpdateEvent() {
		// given
		boolean isActive = true;

		// when
		scenarioEventPublisher.publishNotificationActiveUpdateEvent(memberId, isActive);

		// then
		verify(eventPublisher).publishEvent(any(ActiveUpdateEvent.class));
	}

	@Test
	void Given_ValidMemberIdAndIsActiveFalse_When_PublishNotifActiveUpdateEvent_Then_PublishActiveUpdateEvent() {
		// given
		boolean isActive = false;

		// when
		scenarioEventPublisher.publishNotificationActiveUpdateEvent(memberId, isActive);

		// then
		verify(eventPublisher).publishEvent(any(ActiveUpdateEvent.class));
	}

	@Test
	void Given_ValidMemberId_When_PublishScenarioOrderUpdateEvent_Then_PublishScenarioOrderUpdateEvent() {
		// given
		// memberId는 클래스 레벨에서 정의됨

		// when
		scenarioEventPublisher.publishScenarioOrderUpdateEvent(memberId);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioOrderUpdateEvent.class));
	}

	@Test
	void Given_ValidMemberAndScenarioAndDate_When_PublishMissionCheckUpdateEvent_Then_PublishMissionCheckUpdateEvent() {
		// given
		LocalDate date = LocalDate.of(2024, 1, 15);

		// when
		scenarioEventPublisher.publishMissionCheckUpdateEvent(memberId, scenarioId, date);

		// then
		verify(eventPublisher).publishEvent(any(MissionCheckUpdateEvent.class));
	}

	@Test
	void Given_ValidParametersForScenarioDelete_When_PublishScenarioDeleteEvent_Then_PublishScenarioDeleteEvent() {
		// given
		Boolean isNotificationActive = true;
		NotificationType notificationType = NotificationType.TIME;

		// when
		scenarioEventPublisher.publishScenarioDeleteEvent(
			memberId, scenarioId, isNotificationActive, notificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioDeleteEvent.class));
	}

	@Test
	void Given_ValidMemberAndScenarioId_When_PublishTodayMissionDeleteEvent_Then_PublishMissionDeleteEvent() {
		// given

		// when
		scenarioEventPublisher.publishTodayMissionDeleteEvent(memberId, scenarioId);

		// then
		verify(eventPublisher).publishEvent(any(MissionDeleteEvent.class));
	}

	@Test
	void Given_NullScenario_When_PublishScenarioCreateEvent_Then_PublishScenarioCreateEvent() {
		// given
		Scenario scenario = null;
		NotificationType notificationType = NotificationType.TIME;

		// when
		scenarioEventPublisher.publishScenarioCreateEvent(memberId, scenario, notificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioCreateEvent.class));
	}

	@Test
	void Given_LocationNotificationType_When_PublishScenarioCreateEvent_Then_PublishScenarioCreateEvent() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.LOCATION)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("위치 기반 시나리오")
			.memo("위치 기반 메모")
			.notification(notification)
			.build();

		NotificationType notificationType = NotificationType.LOCATION;

		// when
		scenarioEventPublisher.publishScenarioCreateEvent(memberId, scenario, notificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioCreateEvent.class));
	}

	@Test
	void Given_DifferentMemberId_When_PublishScenarioCreateEvent_Then_PublishScenarioCreateEvent() {
		// given
		Long differentMemberId = 999L;
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("다른 사용자 시나리오")
			.memo("다른 사용자 메모")
			.notification(notification)
			.build();

		NotificationType notificationType = NotificationType.TIME;

		// when
		scenarioEventPublisher.publishScenarioCreateEvent(differentMemberId, scenario, notificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioCreateEvent.class));
	}

	@Test
	void Given_FalseNotificationActive_When_PublishScenarioDeleteEvent_Then_PublishScenarioDeleteEvent() {
		// given
		Boolean isNotificationActive = false;
		NotificationType notificationType = NotificationType.TIME;

		// when
		scenarioEventPublisher.publishScenarioDeleteEvent(
			memberId, scenarioId, isNotificationActive, notificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioDeleteEvent.class));
	}

	@Test
	void Given_FalseOldScenarioNotificationActive_When_PublishScenarioUpdateEvent_Then_PublishScenarioUpdateEvent() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("비활성화된 시나리오")
			.memo("비활성화된 메모")
			.notification(notification)
			.build();

		Boolean isOldScenarioNotificationActive = false;
		NotificationType newNotificationType = NotificationType.TIME;

		// when
		scenarioEventPublisher.publishScenarioUpdateEvent(
			memberId, scenario, isOldScenarioNotificationActive, newNotificationType);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioUpdateEvent.class));
	}

	@Test
	void Given_PastDate_When_PublishMissionCreateEvent_Then_PublishMissionCreateEvent() {
		// given
		LocalDate pastDate = LocalDate.of(2023, 12, 1);

		// when
		scenarioEventPublisher.publishMissionCreateEvent(memberId, scenarioId, pastDate);

		// then
		verify(eventPublisher).publishEvent(any(MissionCreateEvent.class));
	}

	@Test
	void Given_FutureDate_When_PublishMissionCheckUpdateEvent_Then_PublishMissionCheckUpdateEvent() {
		// given
		LocalDate futureDate = LocalDate.of(2025, 12, 31);

		// when
		scenarioEventPublisher.publishMissionCheckUpdateEvent(memberId, scenarioId, futureDate);

		// then
		verify(eventPublisher).publishEvent(any(MissionCheckUpdateEvent.class));
	}

}
