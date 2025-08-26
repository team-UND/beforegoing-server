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
	public void handleDelete(ScenarioDeleteEvent event) {
		Long memberId = event.memberId();
		Long scenarioId = event.scenarioId();
		Boolean isNotificationActive = event.isNotificationActive();

		try {
			if (!isNotificationActive) {
				return;
			}
			processWithNotification(memberId, scenarioId);
			log.info("Deleted notification cache for scenarioId={}", scenarioId);
		} catch (Exception e) {
			log.error("Failed to process scenario delete event: {}", event, e);
			// 실패 시 캐시 삭제 (동기화 실패 대비)
			notificationCacheService.deleteCache(memberId);
		}
	}

	private void processWithNotification(Long memberId, Long scenarioId) {
		notificationCacheService.deleteFromCache(memberId, scenarioId);
	}

}

