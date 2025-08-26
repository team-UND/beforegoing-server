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
	public void handleUpdate(ScenarioUpdateEvent event) {
		Long memberId = event.memberId();
		Scenario scenario = event.scenario();
		Notification notification = scenario.getNotification();

		try {
			if (notification == null || !notification.isActive()) {
				processWithoutNotification(memberId, scenario);
				return;
			}
			processWithNotification(memberId, scenario);
		} catch (Exception e) {
			log.error("Failed to process scenario update event: {}", event, e);
			// 실패 시 캐시 삭제 (동기화 실패 대비)
			notificationCacheService.deleteCache(memberId);
		}
	}

	private void processWithNotification(Long memberId, Scenario scenario) {
		notificationCacheService.deleteFromCache(memberId, scenario.getId());
		notificationCacheService.updateCache(memberId, scenario);
	}

	private void processWithoutNotification(Long memberId, Scenario scenario) {
		notificationCacheService.deleteFromCache(memberId, scenario.getId());
	}

}

