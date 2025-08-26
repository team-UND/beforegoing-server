package com.und.server.scenario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.repository.ScenarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioNotificationService {

	private final NotificationService notificationService;
	private final ScenarioRepository scenarioRepository;

	public List<ScenarioNotificationResponse> getScenarioNotifications(Long memberId) {
		List<Scenario> scenarios = scenarioRepository.findByMemberId(memberId);

		List<ScenarioNotificationResponse> scenarioNotifications = new ArrayList<>();
		for (Scenario scenario : scenarios) {
			if (scenario.getNotification() != null && scenario.getNotification().isActive()) {
				ScenarioNotificationResponse response = convertToResponse(scenario);
				scenarioNotifications.add(response);
			}
		}

		return scenarioNotifications;
	}

	//todo N+1문제가 발생하니까 jqpl로 그냥 일어서 가져오는건?
	private ScenarioNotificationResponse convertToResponse(Scenario scenario) {
		NotificationConditionResponse condition =
			notificationService.findNotificationDetails(scenario.getNotification());

		return ScenarioNotificationResponse.from(scenario, condition);
	}

}
