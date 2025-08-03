package com.und.server.scenario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.NotificationInfoDto;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGrouper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScenarioService {

	private final NotificationService notificationService;
	private final ScenarioRepository scenarioRepository;
	private final MissionTypeGrouper missionTypeGrouper;


	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(Long memberId) {
		List<Scenario> scenarioList = scenarioRepository.findByMemberIdOrderByOrder(memberId);

		return ScenarioResponse.listOf(scenarioList);
	}


	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioByScenarioId(Long memberId, Long scenarioId) {
		Scenario scenario = scenarioRepository.findById(scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		Member member = scenario.getMember();
		if (!memberId.equals(member.getId())) {
			throw new ServerException(ScenarioErrorResult.UNAUTHORIZED_ACCESS);
		}

		List<Mission> groupdBasicMissionList =
			missionTypeGrouper.groupAndSortByType(scenario.getMissionList(), MissionType.BASIC);

		Notification notification = scenario.getNotification();
		if (!notification.isActive()) {
			return ScenarioDetailResponse.of(
				scenario,
				groupdBasicMissionList,
				null
			);
		}

		NotificationInfoDto notifInfo = notificationService.findNotificationDetails(notification);

		return ScenarioDetailResponse.of(
			scenario,
			groupdBasicMissionList,
			notifInfo
		);
	}

}
