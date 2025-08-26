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
public class ScenarioCreateEventListener {

	private final NotificationCacheService notificationCacheService;

	@Async
	@TransactionalEventListener
	public void handleCreate(ScenarioCreateEvent event) {
		Long memberId = event.memberId();
		Scenario scenario = event.scenario();
		Notification notification = scenario.getNotification();

		try {
			if (notification == null || !notification.isActive()) {
				return;
			}
			processWithNotification(memberId, scenario);

		} catch (Exception e) {
			log.error("Failed to process scenario create event: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	private void processWithNotification(Long memberId, Scenario scenario) {
		notificationCacheService.updateCache(memberId, scenario);
	}

}

