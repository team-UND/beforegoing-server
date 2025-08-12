package com.und.server.scenario.dto.response;

import java.util.List;

import com.und.server.scenario.entity.Mission;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Home display Mission group by Mission type response")
public record MissionGroupResponse(

	@ArraySchema(
		arraySchema = @Schema(description = "Basic type mission list, Sort in order"),
		schema = @Schema(implementation = MissionResponse.class), maxItems = 20
	)
	List<MissionResponse> basicMissionList,

	@ArraySchema(
		arraySchema = @Schema(description = "Today type mission list, Sort in order of created date"),
		schema = @Schema(implementation = MissionResponse.class), maxItems = 20
	)
	List<MissionResponse> todayMissionList

) {

	public static MissionGroupResponse from(List<Mission> basic, List<Mission> today) {
		return new MissionGroupResponse(
			MissionResponse.listFrom(basic),
			MissionResponse.listFrom(today)
		);
	}

}
