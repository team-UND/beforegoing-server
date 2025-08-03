package com.und.server.scenario.dto.response;

import java.util.List;

import com.und.server.scenario.entity.Mission;

public record MissionGroupResponse(
	List<MissionResponse> basicMissionList,
	List<MissionResponse> todayMissionList
) {

	public static MissionGroupResponse of(List<Mission> basic, List<Mission> today) {
		return new MissionGroupResponse(
			MissionResponse.listOf(basic),
			MissionResponse.listOf(today)
		);
	}

}
