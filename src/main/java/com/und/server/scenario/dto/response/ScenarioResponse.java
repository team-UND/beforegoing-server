package com.und.server.scenario.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.scenario.entity.Scenario;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScenarioResponse {

	private Long scenarioId;
	private String scenarioName;
	private String memo;
	private Integer order;


	public static ScenarioResponse of(Scenario scenario) {
		return ScenarioResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.order(scenario.getOrder())
			.build();
	}

	public static List<ScenarioResponse> listOf(List<Scenario> scenarioList) {
		return scenarioList.stream()
			.map(ScenarioResponse::of)
			.toList();
	}

}
