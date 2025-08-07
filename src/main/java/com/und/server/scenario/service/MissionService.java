package com.und.server.scenario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.MissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGrouper;
import com.und.server.scenario.util.OrderCalculator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MissionService {

	private final MissionRepository missionRepository;
	private final MissionTypeGrouper missionTypeGrouper;


	@Transactional(readOnly = true)
	public MissionGroupResponse findMissionsByScenarioId(Long memberId, Long scenarioId) {
		List<Mission> missionList = missionRepository.findAllByScenarioId(scenarioId);

		if (missionList == null || missionList.isEmpty()) {
			return MissionGroupResponse.of(List.of(), List.of());
		}
		for (Mission mission : missionList) {
			Member member = mission.getScenario().getMember();
			if (!memberId.equals(member.getId())) {
				throw new ServerException(ScenarioErrorResult.UNAUTHORIZED_ACCESS);
			}
		}

		List<Mission> groupedBasicMissionList =
			missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC);
		List<Mission> groupedTodayMissionList =
			missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY);

		return MissionGroupResponse.of(groupedBasicMissionList, groupedTodayMissionList);
	}


	@Transactional
	public void addTodayMission(Scenario scenario, TodayMissionRequest missionAddInfo) {
		Mission newMission = Mission.builder()
			.scenario(scenario)
			.content(missionAddInfo.content())
			.isChecked(false)
			.missionType(MissionType.TODAY)
			.build();

		missionRepository.save(newMission);
	}


	@Transactional
	public void addBasicMission(Scenario scenario, List<MissionRequest> missionInfoList) {
		if (missionInfoList.isEmpty()) {
			return;
		}

		List<Mission> missionList = new ArrayList<>();
		int order = OrderCalculator.START_ORDER;

		for (MissionRequest missionInfo : missionInfoList) {
			missionList.add(missionInfo.toEntity(scenario, order));
			order += OrderCalculator.DEFAULT_ORDER;
		}

		missionRepository.saveAll(missionList);
	}

//	public void updateBasicMission(Scenario scenario, List<MissionRequest> missionInfoList) {}

}
