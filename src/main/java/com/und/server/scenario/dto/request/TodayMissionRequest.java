package com.und.server.scenario.dto.request;

import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Today type Mission request")
public record TodayMissionRequest(

	@Schema(description = "Mission content", example = "Lock the door")
	@NotBlank(message = "Content must not be blank")
	@Size(max = 10, message = "Content must be at most 10 characters")
	String content

) {

	public Mission toEntity(Scenario scenario) {
		return Mission.builder()
			.scenario(scenario)
			.content(content)
			.isChecked(false)
			.missionType(MissionType.TODAY)
			.build();
	}

}
