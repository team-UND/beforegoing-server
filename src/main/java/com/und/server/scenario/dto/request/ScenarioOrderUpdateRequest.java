package com.und.server.scenario.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Scenario order update request")
public class ScenarioOrderUpdateRequest {

	@Schema(description = "Previous Scenario order", example = "1000")
	@Min(value = 0, message = "prevOrder must be greater than or equal to 1")
	private Integer prevOrder;

	@Schema(description = "Next Scenario order", example = "3000")
	@Min(value = 0, message = "nextOrder must be greater than or equal to 1")
	private Integer nextOrder;

}
