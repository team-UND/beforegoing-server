package com.und.server.notification.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.und.server.notification.service.NotificationCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioDeleteEventListener {

	private final NotificationCacheService notificationCacheService;

	@Async
	@TransactionalEventListener
	public void handleDelete(final ScenarioDeleteEvent event) {
		final Long memberId = event.memberId();
		final Long scenarioId = event.scenarioId();
		final Boolean isNotificationActive = event.isNotificationActive();

		try {
			if (!isNotificationActive) {
				return;
			}
			processWithNotification(memberId, scenarioId);

		} catch (Exception e) {
			log.error("Failed to process scenario delete event: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	private void processWithNotification(final Long memberId, final Long scenarioId) {
		notificationCacheService.deleteCache(memberId, scenarioId);
	}

}

