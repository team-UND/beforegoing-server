package com.und.server.scenario.util;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.exception.ScenarioErrorResult;

@Component
public class MissionTypeGroupSorter {

	public List<Mission> groupAndSortByType(final List<Mission> missions, final MissionType missionType) {
		if (missionType == null) {
			throw new ServerException(ScenarioErrorResult.UNSUPPORTED_MISSION_TYPE);
		}
		if (missions == null || missions.isEmpty()) {
			return missions;
		}

		return missions.stream()
			.filter(m -> m.getMissionType() == missionType)
			.sorted(getComparatorByType(missionType))
			.toList();
	}

	private Comparator<Mission> getComparatorByType(final MissionType type) {
		return switch (type) {

			case BASIC -> Comparator.comparing(Mission::getMissionOrder,
				Comparator.nullsLast(Comparator.naturalOrder()));
			case TODAY -> Comparator.comparing(Mission::getCreatedAt).reversed();

			default -> throw new ServerException(ScenarioErrorResult.UNSUPPORTED_MISSION_TYPE);
		};
	}

}
