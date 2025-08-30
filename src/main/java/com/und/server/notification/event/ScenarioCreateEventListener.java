package com.und.server.notification.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationCacheException;
import com.und.server.notification.service.NotificationCacheService;
import com.und.server.scenario.entity.Scenario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScenarioCreateEventListener {

	private final NotificationCacheService notificationCacheService;

	@Async
	@TransactionalEventListener
	public void handleCreate(final ScenarioCreateEvent event) {
		final Long memberId = event.memberId();
		final Scenario scenario = event.scenario();
		final Notification notification = scenario.getNotification();

		try {
			if (notification == null || !notification.isActive()) {
				return;
			}
			processWithNotification(memberId, scenario);

		} catch (NotificationCacheException e) {
			log.error("Failed to process scenario create event due to cache error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		} catch (Exception e) {
			log.error("Failed to process scenario create event due to an unexpected error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	private void processWithNotification(final Long memberId, final Scenario scenario) {
		notificationCacheService.updateCache(memberId, scenario);
	}

}
