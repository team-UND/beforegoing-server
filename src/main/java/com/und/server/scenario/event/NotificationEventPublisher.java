package com.und.server.scenario.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.und.server.scenario.entity.Scenario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishCreateEvent(final Long memberId, final Scenario scenario) {
		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);

		eventPublisher.publishEvent(event);
	}

	public void publishUpdateEvent(
		final Long memberId, final Scenario scenario, final Boolean isOldScenarioNotificationActive
	) {
		ScenarioUpdateEvent event =
			new ScenarioUpdateEvent(memberId, scenario, isOldScenarioNotificationActive);

		eventPublisher.publishEvent(event);
	}

	public void publishDeleteEvent(
		final Long memberId, final Long scenarioId, final Boolean isNotificationActive
	) {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(memberId, scenarioId, isNotificationActive);

		eventPublisher.publishEvent(event);
	}

	public void publishActiveUpdateEvent(final Long memberId, final boolean isActive) {
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, isActive);

		eventPublisher.publishEvent(event);
	}

}
