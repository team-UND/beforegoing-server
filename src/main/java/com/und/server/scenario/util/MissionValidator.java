package com.und.server.scenario.util;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionSearchType;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.exception.ScenarioErrorResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MissionValidator {

	private static final int BASIC_MISSION_MAX_COUNT = 20;
	private static final int TODAY_MISSION_MAX_COUNT = 20;

	public void validateMissionAccessibleMember(Mission mission, Long memberId) {
		Member member = mission.getScenario().getMember();
		if (!memberId.equals(member.getId())) {
			throw new ServerException(ScenarioErrorResult.UNAUTHORIZED_ACCESS);
		}
	}

	public void validateMaxBasicMissionCount(List<Mission> missions) {
		if (missions.size() >= BASIC_MISSION_MAX_COUNT) {
			throw new ServerException(ScenarioErrorResult.MAX_MISSION_COUNT_EXCEEDED);
		}
	}

	public void validateMaxTodayMissionCount(List<Mission> missions) {
		if (missions.size() >= TODAY_MISSION_MAX_COUNT) {
			throw new ServerException(ScenarioErrorResult.MAX_MISSION_COUNT_EXCEEDED);
		}
	}

	public void validateTodayMissionDateRange(LocalDate today, LocalDate requestDate) {
		MissionSearchType missionSearchType = MissionSearchType.getMissionSearchType(today, requestDate);
		if (missionSearchType == MissionSearchType.PAST) {
			throw new ServerException(ScenarioErrorResult.INVALID_TODAY_MISSION_DATE);
		}
	}

}
