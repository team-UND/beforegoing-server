package com.und.server.scenario.dto;

import java.util.List;

import com.und.server.scenario.entity.Mission;

import lombok.Builder;

@Builder
public record MissionUpdatePlanDto(

	List<Mission> missionsToSave,
	List<Long> missionsToDelete

) { }
