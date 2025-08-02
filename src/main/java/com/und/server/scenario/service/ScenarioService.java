package com.und.server.scenario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.member.entity.Member;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.repository.ScenarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScenarioService {

	private final NotificationService notificationService;
	private final ScenarioRepository scenarioRepository;

	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(Long memberId) {
		List<Scenario> scenarioList = scenarioRepository.findByMemberIdOrderByOrder(memberId);

		return ScenarioResponse.listOf(scenarioList);
	}

	/// memberId로 검증하는 부분을 모듈화할수없나 커스텀 어노테이션?
	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioByScenarioId(Long memberId, Long scenarioId) {
		Scenario scenario = scenarioRepository.findById(scenarioId)
			.orElseThrow(() -> new IllegalArgumentException());

		Member member = scenario.getMember();
		if (!memberId.equals(member.getId())) {
			throw new IllegalArgumentException();
		}

		Notification notification = scenario.getNotification();
		if (!notification.isActive()) {
			return ScenarioDetailResponse.of(scenario, List.of());
		}
		List<NotificationDetailResponse> notifDetailList = notificationService.findNotificationDetails(notification);

		return ScenarioDetailResponse.of(scenario, notifDetailList);
	}

}
