package com.und.server.scenario.event.publisher;

import java.time.LocalDate;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.event.ActiveUpdateEvent;
import com.und.server.scenario.event.MissionCheckUpdateEvent;
import com.und.server.scenario.event.MissionCreateEvent;
import com.und.server.scenario.event.MissionDeleteEvent;
import com.und.server.scenario.event.ScenarioCreateEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioOrderUpdateEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScenarioEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishScenarioCreateEvent(
		final Long memberId, final Scenario scenario, final NotificationType notificationType
	) {
		final ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario, notificationType);

		eventPublisher.publishEvent(event);
	}

	public void publishMissionCreateEvent(
		final Long memberId, final Long scenarioId, final LocalDate date
	) {
		final MissionCreateEvent event = new MissionCreateEvent(memberId, scenarioId, date);

		eventPublisher.publishEvent(event);
	}

	public void publishScenarioUpdateEvent(
		final Long memberId,
		final Scenario scenario,
		final Boolean isOldScenarioNotificationActive,
		final NotificationType newNotificationType
	) {
		final ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			memberId, scenario, isOldScenarioNotificationActive, newNotificationType);

		eventPublisher.publishEvent(event);
	}

	public void publishNotificationActiveUpdateEvent(final Long memberId, final boolean isActive) {
		final ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, isActive);

		eventPublisher.publishEvent(event);
	}

	public void publishScenarioOrderUpdateEvent(final Long memberId) {
		final ScenarioOrderUpdateEvent event = new ScenarioOrderUpdateEvent(memberId);

		eventPublisher.publishEvent(event);
	}

	public void publishMissionCheckUpdateEvent(
		final Long memberId, final Long scenarioId, final LocalDate date
	) {
		final MissionCheckUpdateEvent event = new MissionCheckUpdateEvent(memberId, scenarioId, date);

		eventPublisher.publishEvent(event);
	}

	public void publishScenarioDeleteEvent(
		final Long memberId,
		final Long scenarioId,
		final Boolean isNotificationActive,
		final NotificationType newNotificationType
	) {
		final ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			memberId, scenarioId, isNotificationActive, newNotificationType);

		eventPublisher.publishEvent(event);
	}

	public void publishTodayMissionDeleteEvent(final Long memberId, final Long scenarioId) {
		final MissionDeleteEvent event = new MissionDeleteEvent(memberId, scenarioId);

		eventPublisher.publishEvent(event);
	}

}
