package com.und.server.scenario.event.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.event.ScenarioCreateEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioOrderUpdateEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;
import com.und.server.scenario.service.ScenarioCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScenarioCacheEvictListener {

	private final ScenarioCacheService scenarioCacheService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onScenarioCreate(final ScenarioCreateEvent event) {
		final Long memberId = event.memberId();
		final NotificationType notificationType = event.notificationType();

		scenarioCacheService.evictUserScenarioCache(memberId, notificationType);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onScenarioUpdate(final ScenarioUpdateEvent event) {
		final Long memberId = event.memberId();
		final NotificationType notificationType = event.newNotificationType();

		scenarioCacheService.evictUserScenarioCache(memberId, notificationType);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onScenarioOrderUpdate(final ScenarioOrderUpdateEvent event) {
		final Long memberId = event.memberId();

		scenarioCacheService.evictUserScenarioCache(memberId);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onScenarioDelete(final ScenarioDeleteEvent event) {
		final Long memberId = event.memberId();
		final NotificationType notificationType = event.notificationType();

		scenarioCacheService.evictUserScenarioCache(memberId, notificationType);
	}

}
