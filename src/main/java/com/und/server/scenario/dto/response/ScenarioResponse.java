package com.und.server.scenario.dto.response;

import java.util.List;

import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Scenario response")
public record ScenarioResponse(

	@Schema(description = "Scenario id", example = "1")
	Long scenarioId,

	@Schema(description = "Scenario name", example = "Bef house")
	String scenarioName,

	@Schema(description = "Scenario memo", example = "Item to carry")
	String memo,

	@Schema(description = "Scenario order", example = "3000")
	Integer scenarioOrder

) {

	public static ScenarioResponse from(Scenario scenario) {
		return ScenarioResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.scenarioOrder(scenario.getScenarioOrder())
			.build();
	}

	public static List<ScenarioResponse> listFrom(List<Scenario> scenarioList) {
		return scenarioList.stream()
			.map(ScenarioResponse::from)
			.toList();
	}

}
