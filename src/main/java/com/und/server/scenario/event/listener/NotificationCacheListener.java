package com.und.server.scenario.event.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationCacheException;
import com.und.server.notification.service.NotificationCacheService;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.event.ActiveUpdateEvent;
import com.und.server.scenario.event.ScenarioCreateEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationCacheListener {

	private final NotificationCacheService notificationCacheService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onCreate(final ScenarioCreateEvent event) {
		final Long memberId = event.memberId();
		final Scenario scenario = event.scenario();
		final Notification notification = scenario.getNotification();

		try {
			if (notification == null || !notification.isActive()) {
				return;
			}
			updateCache(memberId, scenario);

		} catch (NotificationCacheException e) {
			log.error("Failed to process scenario create event due to cache error: {}", event, e);
			deleteMemberAllCache(memberId);
		} catch (Exception e) {
			log.error("Failed to process scenario create event due to an unexpected error: {}", event, e);
			deleteMemberAllCache(memberId);
		}
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onUpdate(final ScenarioUpdateEvent event) {
		final Long memberId = event.memberId();
		final Boolean isOldScenarioNotificationActive = event.isOldScenarioNotificationActive();
		final Scenario updatedScenario = event.updatedScenario();
		final Notification notification = updatedScenario.getNotification();

		try {
			if (notification == null || !notification.isActive()) {
				if (!isOldScenarioNotificationActive) {
					return;
				}
				deleteCache(memberId, updatedScenario.getId());
				return;
			}
			updateCache(memberId, updatedScenario);

		} catch (NotificationCacheException e) {
			log.error("Failed to process scenario update event due to cache error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		} catch (Exception e) {
			log.error("Failed to process scenario update event due to an unexpected error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	@Async
	@TransactionalEventListener
	public void onActiveUpdate(final ActiveUpdateEvent event) {
		final Long memberId = event.memberId();
		final boolean isActive = event.isActive();

		try {
			if (isActive) {
				refreshCache(memberId);
			} else {
				deleteMemberAllCache(memberId);
			}
		} catch (Exception e) {
			log.error("Failed to process notification active update event due to an unexpected error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onDelete(final ScenarioDeleteEvent event) {
		final Long memberId = event.memberId();
		final Long scenarioId = event.scenarioId();
		final Boolean isNotificationActive = event.isNotificationActive();

		try {
			if (!isNotificationActive) {
				return;
			}
			deleteCache(memberId, scenarioId);

		} catch (NotificationCacheException e) {
			log.error("Failed to process scenario delete event due to cache error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		} catch (Exception e) {
			log.error("Failed to process scenario delete event due to an unexpected error: {}", event, e);
			notificationCacheService.deleteMemberAllCache(memberId);
		}
	}

	private void updateCache(final Long memberId, final Scenario scenario) {
		notificationCacheService.updateCache(memberId, scenario);
	}

	private void deleteCache(final Long memberId, final Long scenarioId) {
		notificationCacheService.deleteCache(memberId, scenarioId);
	}

	private void deleteMemberAllCache(final Long memberId) {
		notificationCacheService.deleteMemberAllCache(memberId);
	}

	private void refreshCache(final Long memberId) {
		notificationCacheService.refreshCacheFromDatabase(memberId);
	}

}
