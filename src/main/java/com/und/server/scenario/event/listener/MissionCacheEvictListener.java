package com.und.server.scenario.event.listener;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.und.server.scenario.event.MissionCheckUpdateEvent;
import com.und.server.scenario.event.MissionCreateEvent;
import com.und.server.scenario.event.MissionDeleteEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;
import com.und.server.scenario.service.MissionCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MissionCacheEvictListener {

	private final MissionCacheService missionCacheService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onMissionCreate(final MissionCreateEvent event) {
		final Long memberId = event.memberId();
		final Long scenarioId = event.scenarioId();
		final LocalDate date = event.date();

		try {
			missionCacheService.evictUserMissionCache(memberId, scenarioId, date);
		} catch (Exception e) {
			log.error("Failed to evict mission cache after mission creation - event: {}", event, e);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onScenarioUpdate(final ScenarioUpdateEvent event) {
		final Long memberId = event.memberId();
		final Long scenarioId = event.updatedScenario().getId();

		try {
			missionCacheService.evictUserMissionCache(memberId, scenarioId);
		} catch (Exception e) {
			log.error("Failed to evict mission cache after scenario update - event: {}", event, e);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onMissionCheckUpdate(final MissionCheckUpdateEvent event) {
		final Long memberId = event.memberId();
		final Long scenarioId = event.scenarioId();
		final LocalDate date = event.date();

		try {
			missionCacheService.evictUserMissionCache(memberId, scenarioId, date);
		} catch (Exception e) {
			log.error("Failed to evict mission cache after mission check update - event: {}", event, e);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onScenarioDelete(final ScenarioDeleteEvent event) {
		final Long memberId = event.memberId();
		final Long scenarioId = event.scenarioId();

		try {
			missionCacheService.evictUserMissionCache(memberId, scenarioId);
		} catch (Exception e) {
			log.error("Failed to evict mission cache after scenario deletion - event: {}", event, e);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onMissionDelete(final MissionDeleteEvent event) {
		final Long memberId = event.memberId();
		final Long scenarioId = event.scenarioId();

		try {
			missionCacheService.evictUserMissionCache(memberId, scenarioId);
		} catch (Exception e) {
			log.error("Failed to evict mission cache after mission deletion - event: {}", event, e);
		}
	}

}
