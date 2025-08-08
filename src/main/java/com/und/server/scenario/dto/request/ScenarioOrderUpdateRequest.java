package com.und.server.scenario.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class ScenarioOrderUpdateRequest {

	@Min(value = 1, message = "prevOrder must be greater than or equal to 1")
	private Integer prevOrder;

	@Min(value = 1, message = "nextOrder must be greater than or equal to 1")
	private Integer nextOrder;

}
