package com.und.server.scenario.repository;

import java.util.List;

import com.und.server.notification.dto.response.ScenarioNotificationResponse;

public interface ScenarioRepositoryCustom {

	List<ScenarioNotificationResponse> findTimeScenarioNotifications(Long memberId);

}
