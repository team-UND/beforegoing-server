package com.und.server.notification.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.und.server.notification.event.ScenarioCreateEvent;
import com.und.server.notification.event.ScenarioDeleteEvent;
import com.und.server.notification.event.ScenarioUpdateEvent;
import com.und.server.scenario.entity.Scenario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 캐시 업데이트 이벤트 발행 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 시나리오 생성 이벤트 발행
	 */
	public void publishCreateEvent(Long memberId, Scenario scenario) {
		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);
		eventPublisher.publishEvent(event);

		log.debug("Published scenario create event: memberId={}, scenarioId={}",
			memberId, scenario.getId());
	}

	/**
	 * 시나리오 수정 이벤트 발행
	 */
	public void publishUpdateEvent(Long memberId, Scenario scenario) {
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario);
		eventPublisher.publishEvent(event);

		log.debug("Published scenario update event: memberId={}, scenarioId={}",
			memberId, scenario.getId());
	}

	/**
	 * 시나리오 삭제 이벤트 발행
	 */
	public void publishDeleteEvent(Long memberId, Long scenarioId, Boolean isNotificationActive) {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(memberId, scenarioId, isNotificationActive);
		eventPublisher.publishEvent(event);

		log.debug("Published scenario delete event: memberId={}, scenarioId={}",
			memberId, scenarioId);
	}
}
