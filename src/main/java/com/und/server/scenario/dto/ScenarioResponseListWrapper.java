package com.und.server.scenario.dto;

import java.io.Serializable;
import java.util.List;

import com.und.server.scenario.dto.response.ScenarioResponse;

public record ScenarioResponseListWrapper(

	List<ScenarioResponse> scenarioResponses

) implements Serializable {

	public static ScenarioResponseListWrapper from(List<ScenarioResponse> scenarioResponses) {
		return new ScenarioResponseListWrapper(scenarioResponses);
	}

}
