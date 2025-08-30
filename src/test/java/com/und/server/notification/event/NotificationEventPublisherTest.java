package com.und.server.notification.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;


@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

	@InjectMocks
	private NotificationEventPublisher notificationEventPublisher;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private final Long memberId = 1L;
	private final Long scenarioId = 1L;


	@Test
	void Given_ValidMemberIdAndScenario_When_PublishCreateEvent_Then_PublishScenarioCreateEvent() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("새로운 루틴")
			.memo("새로운 메모")
			.notification(notification)
			.build();

		// when
		notificationEventPublisher.publishCreateEvent(memberId, scenario);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioCreateEvent.class));
	}


	@Test
	void Given_ValidMemberIdAndScenario_When_PublishUpdateEvent_Then_PublishScenarioUpdateEvent() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("업데이트된 루틴")
			.memo("업데이트된 메모")
			.notification(notification)
			.build();

		Boolean isOldScenarioNotificationActive = false;

		// when
		notificationEventPublisher.publishUpdateEvent(memberId, scenario,
			isOldScenarioNotificationActive);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioUpdateEvent.class));
	}


	@Test
	void Given_ValidMemberIdAndScenarioId_When_PublishDeleteEvent_Then_PublishScenarioDeleteEvent() {
		// given
		Boolean isNotificationActive = true;

		// when
		notificationEventPublisher.publishDeleteEvent(memberId, scenarioId,
			isNotificationActive);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioDeleteEvent.class));
	}


	@Test
	void Given_ValidMemberIdAndScenarioWithNullNotification_When_PublishCreateEvent_Then_PublishScenarioCreateEvent() {
		// given
		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("알림 없는 루틴")
			.memo("알림 없는 메모")
			.notification(null)
			.build();

		// when
		notificationEventPublisher.publishCreateEvent(memberId, scenario);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioCreateEvent.class));
	}


	@Test
	void Given_ValidMemberIdAndScenarioWithInactiveNoti_When_PublishUpdateEvent_Then_PublishScenarioUpdateEvent() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("비활성화된 루틴")
			.memo("비활성화된 메모")
			.notification(notification)
			.build();

		Boolean isOldScenarioNotificationActive = true;

		// when
		notificationEventPublisher.publishUpdateEvent(memberId, scenario,
			isOldScenarioNotificationActive);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioUpdateEvent.class));
	}


	@Test
	void Given_ValidMemberIdAndScenarioIdWithInactiveNoti_When_PublishDeleteEvent_Then_PublishScenarioDeleteEvent() {
		// given
		Boolean isNotificationActive = false;

		// when
		notificationEventPublisher.publishDeleteEvent(memberId, scenarioId,
			isNotificationActive);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioDeleteEvent.class));
	}


	@Test
	void Given_DifferentMemberIdAndScenarioId_When_PublishDeleteEvent_Then_PublishScenarioDeleteEvent() {
		// given
		Long differentMemberId = 2L;
		Long differentScenarioId = 3L;
		Boolean isNotificationActive = true;

		// when
		notificationEventPublisher.publishDeleteEvent(differentMemberId, differentScenarioId,
			isNotificationActive);

		// then
		verify(eventPublisher).publishEvent(any(ScenarioDeleteEvent.class));
	}

}
