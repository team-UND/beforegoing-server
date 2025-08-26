package com.und.server.scenario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.scenario.repository.ScenarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScenarioNotificationService {

	private final ScenarioRepository scenarioRepository;

	public List<ScenarioNotificationResponse> getScenarioNotifications(Long memberId) {
		List<ScenarioNotificationResponse> scenarioNotifications = new ArrayList<>();

		for (NotificationType type : NotificationType.values()) {
			switch (type) {
				case TIME -> scenarioNotifications.addAll(
					scenarioRepository.findTimeScenarioNotifications(memberId));
			}
		}
		System.out.println("db 다시 조회함");
		return scenarioNotifications;
	}

}
