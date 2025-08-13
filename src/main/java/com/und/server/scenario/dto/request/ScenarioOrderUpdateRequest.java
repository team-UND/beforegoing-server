package com.und.server.scenario.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
@Schema(description = "Scenario order update request")
public record ScenarioOrderUpdateRequest(

	@Schema(description = "Previous Scenario order", example = "1000")
	@Min(value = 0, message = "prevOrder must be greater than or equal to 1")
	Integer prevOrder,

	@Schema(description = "Next Scenario order", example = "2000")
	@Min(value = 0, message = "nextOrder must be greater than or equal to 1")
	Integer nextOrder

) { }
