package com.und.server.scenario.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScenarioValidator {

	private static final int SCENARIO_MAX_COUNT = 20;
	private final ScenarioRepository scenarioRepository;

	public void validateScenarioExists(Long scenarioId) {
		if (!scenarioRepository.existsById(scenarioId)) {
			throw new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO);
		}
	}

	public void validateMaxScenarioCount(List<Integer> orderList) {
		if (orderList.size() >= SCENARIO_MAX_COUNT) {
			throw new ServerException(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED);
		}
	}

}
