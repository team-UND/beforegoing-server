package com.und.server.notification.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationCacheService;
import com.und.server.scenario.entity.Scenario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioUpdateEventListener {

	private final NotificationCacheService notificationCacheService;

	@Async
	@TransactionalEventListener
	public void handleUpdate(final ScenarioUpdateEvent event) {
		final Long memberId = event.memberId();
		final Boolean isOldScenarioNotificationActive = event.isOldScenarioNotificationActive();
		final Scenario updatedScenario = event.updatedScenario();
		final Notification notification = updatedScenario.getNotification();

		try {
			if (notification == null || !notification.isActive()) {
				if (!isOldScenarioNotificationActive) {
					return;
				}
				processWithoutNotification(memberId, updatedScenario);
				return;
			}
			processWithNotification(memberId, updatedScenario);

		} catch (Exception e) {
			log.error("Failed to process scenario update event: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	private void processWithNotification(final Long memberId, final Scenario scenario) {
		notificationCacheService.deleteCache(memberId, scenario.getId());
		notificationCacheService.updateCache(memberId, scenario);
	}

	private void processWithoutNotification(final Long memberId, final Scenario scenario) {
		notificationCacheService.deleteCache(memberId, scenario.getId());
	}

}

