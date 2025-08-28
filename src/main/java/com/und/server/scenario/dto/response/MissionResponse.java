package com.und.server.scenario.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "All Type Mission response")
public record MissionResponse(

	@Schema(description = "Mission id", example = "1")
	Long missionId,

	@Schema(description = "Mission content", example = "Lock door")
	String content,

	@Schema(description = "Check box check display status", example = "true")
	Boolean isChecked,

	@Schema(description = "Mission type", example = "BASIC")
	MissionType missionType

) {

	public static MissionResponse from(final Mission mission) {
		return MissionResponse.builder()
			.missionId(mission.getId())
			.content(mission.getContent())
			.isChecked(mission.getIsChecked())
			.missionType(mission.getMissionType())
			.build();
	}

	public static List<MissionResponse> listFrom(final List<Mission> missionList) {
		if (missionList == null || missionList.isEmpty()) {
			return new ArrayList<>();
		}
		return missionList.stream()
			.map(MissionResponse::from)
			.toList();
	}

}
