package com.und.server.scenario.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

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

}
