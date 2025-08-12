package com.und.server.scenario.dto.response;

import java.util.List;

import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Home display scenario response")
public record HomeScenarioResponse(

	@Schema(description = "Scenario id", example = "1")
	Long scenarioId,

	@Schema(description = "Scenario name", example = "Before house")
	String scenarioName

) {

	public static HomeScenarioResponse from(Scenario scenario) {
		return HomeScenarioResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.build();
	}

	public static List<HomeScenarioResponse> listFrom(List<Scenario> scenarioList) {
		return scenarioList.stream()
			.map(HomeScenarioResponse::from)
			.toList();
	}

}
