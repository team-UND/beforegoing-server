package com.und.server.scenario.util;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.exception.ScenarioErrorResult;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class MissionTypeGrouper {

	public List<Mission> groupAndSortByType(List<Mission> missionList, MissionType missionType) {
		if (missionType == null) {
			throw new ServerException(ScenarioErrorResult.UNSUPPORTED_MISSION_TYPE);
		}
		if (missionList == null || missionList.isEmpty()) {
			return missionList;
		}

		return missionList.stream()
			.filter(m -> m.getMissionType() == missionType)
			.sorted(getComparatorByType(missionType))
			.toList();
	}

	private Comparator<Mission> getComparatorByType(MissionType type) {
		return switch (type) {

			case BASIC -> Comparator.comparing(Mission::getOrder);
			case TODAY -> Comparator.comparing(Mission::getCreatedAt).reversed();

			default -> throw new ServerException(ScenarioErrorResult.UNSUPPORTED_MISSION_TYPE);
		};
	}

}
