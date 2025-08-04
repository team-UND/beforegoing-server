package com.und.server.scenario.dto.requeset;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import com.und.server.scenario.constants.MissionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MissionAddRequest(

	@NotBlank(message = "Content must not be blank")
	@Size(max = 10, message = "Content must be at most 10 characters")
	String content,

	@NotNull(message = "MissionType must not be null")
	MissionType missionType

) {
}
