package com.und.server.scenario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGrouper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MissionService {

	private final MissionRepository missionRepository;
	private final MissionTypeGrouper missionTypeGrouper;

	@Transactional(readOnly = true)
	public MissionGroupResponse findMissionsByScenarioId(Long memberId, Long scenarioId) {
		List<Mission> missionList = missionRepository.findAllByScenarioId(scenarioId);

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

}
