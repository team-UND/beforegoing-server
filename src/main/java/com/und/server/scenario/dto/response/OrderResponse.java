package com.und.server.scenario.dto.response;

import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Scenario order update response")
public record OrderResponse(

	@Schema(description = "Scenario id", example = "1")
	Long id,

	@Schema(description = "Updated Scenario order", example = "2500")
	Integer newOrder

) {

	public static OrderResponse from(final Scenario scenario) {
		return OrderResponse.builder()
			.id(scenario.getId())
			.newOrder(scenario.getScenarioOrder())
			.build();
	}

}
