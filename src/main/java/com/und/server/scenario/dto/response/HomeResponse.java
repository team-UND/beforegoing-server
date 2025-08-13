package com.und.server.scenario.dto.response;

import java.util.List;

import com.und.server.notification.dto.response.NotificationResponse;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Home display response")
public record HomeResponse(

	@ArraySchema(
		arraySchema = @Schema(description = "Scenario list, Sort in order"),
		schema = @Schema(implementation = HomeScenarioResponse.class), maxItems = 20
	)
	List<HomeScenarioResponse> scenarios,

	@Schema(
		description = "Mission list by mission type",
		implementation = NotificationResponse.class
	)
	MissionGroupResponse missionListByType

) { }
