package com.und.server.notification.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.und.server.notification.service.NotificationCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActiveUpdateEventListener {

	private final NotificationCacheService notificationCacheService;

	@Async
	@TransactionalEventListener
	public void handleActiveUpdate(final ActiveUpdateEvent event) {
		final Long memberId = event.memberId();
		final boolean isActive = event.isActive();

		try {
			if (isActive) {
				processWithNotification(memberId);
			} else {
				processWithoutNotification(memberId);
			}
		} catch (Exception e) {
			log.error("Failed to process notification active update event due to an unexpected error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	private void processWithNotification(Long memberId) {
		notificationCacheService.refreshCacheFromDatabase(memberId);
	}

	private void processWithoutNotification(Long memberId) {
		notificationCacheService.deleteMemberAllCache(memberId);
	}

}
