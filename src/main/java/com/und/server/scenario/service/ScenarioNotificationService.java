package com.und.server.scenario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.scenario.repository.ScenarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioNotificationService {

	private final ScenarioRepository scenarioRepository;

	public List<ScenarioNotificationResponse> getScenarioNotifications(final Long memberId) {
		List<ScenarioNotificationResponse> scenarioNotifications = new ArrayList<>();

		for (NotificationType type : NotificationType.values()) {
			switch (type) {
				case TIME -> scenarioNotifications.addAll(
					scenarioRepository.findTimeScenarioNotifications(memberId));
			}
		}
		return scenarioNotifications;
	}

}
