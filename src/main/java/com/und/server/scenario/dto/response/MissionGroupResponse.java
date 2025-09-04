package com.und.server.scenario.dto.response;

import java.util.List;

import com.und.server.scenario.entity.Mission;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Home display Mission group by Mission type response")
public record MissionGroupResponse(

	@Schema(description = "Scenario id", example = "1")
	Long scenarioId,

	@ArraySchema(
		arraySchema = @Schema(description = "Basic type mission list, Sort in order"),
		schema = @Schema(implementation = MissionResponse.class), maxItems = 20
	)
	List<MissionResponse> basicMissions,

	@ArraySchema(
		arraySchema = @Schema(description = "Today type mission list, Sort in order of created date"),
		schema = @Schema(implementation = MissionResponse.class), maxItems = 20
	)
	List<MissionResponse> todayMissions

) {

	public static MissionGroupResponse from(final List<Mission> basic, final List<Mission> today) {
		return MissionGroupResponse.builder()
			.basicMissions(MissionResponse.listFrom(basic))
			.todayMissions(MissionResponse.listFrom(today))
			.build();
	}

	public static MissionGroupResponse from(
		final Long scenarioId, final List<Mission> basic, final List<Mission> today
	) {
		return MissionGroupResponse.builder()
			.scenarioId(scenarioId)
			.basicMissions(MissionResponse.listFrom(basic))
			.todayMissions(MissionResponse.listFrom(today))
			.build();
	}

	public static MissionGroupResponse futureFrom(
		final Long scenarioId, final List<MissionResponse> futureBasic, final List<Mission> today
	) {
		return MissionGroupResponse.builder()
			.scenarioId(scenarioId)
			.basicMissions(futureBasic)
			.todayMissions(MissionResponse.listFrom(today))
			.build();
	}

}
