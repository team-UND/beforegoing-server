package com.und.server.scenario.constants;

import java.time.LocalDate;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.exception.ScenarioErrorResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MissionSearchType {

	TODAY(0),
	PAST(14),
	FUTURE(14);

	private final int rangeDays;

	public static MissionSearchType getMissionSearchType(LocalDate today, LocalDate requestDate) {
		if (requestDate == null || today.isEqual(requestDate)) {
			return TODAY;
		}

		if (requestDate.isBefore(today)) {
			if (requestDate.isBefore(today.minusDays(PAST.getRangeDays()))) {
				throw new ServerException(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE);
			}
			return PAST;
		}

		if (requestDate.isAfter(today)) {
			if (requestDate.isAfter(today.plusDays(FUTURE.getRangeDays()))) {
				throw new ServerException(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE);
			}
			return FUTURE;
		}

		throw new ServerException(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE);
	}

}
