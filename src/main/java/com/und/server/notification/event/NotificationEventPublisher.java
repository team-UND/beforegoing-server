package com.und.server.notification.event;

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

	public void publishCreateEvent(Long memberId, Scenario scenario) {
		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);

		eventPublisher.publishEvent(event);
	}

	public void publishUpdateEvent(
		Long memberId, Scenario scenario, Boolean isOldScenarioNotificationActive
	) {
		ScenarioUpdateEvent event =
			new ScenarioUpdateEvent(memberId, scenario, isOldScenarioNotificationActive);

		eventPublisher.publishEvent(event);
	}

	public void publishDeleteEvent(Long memberId, Long scenarioId, Boolean isNotificationActive) {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(memberId, scenarioId, isNotificationActive);

		eventPublisher.publishEvent(event);
	}

}
