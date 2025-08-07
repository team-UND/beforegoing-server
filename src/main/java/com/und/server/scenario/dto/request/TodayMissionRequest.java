package com.und.server.scenario.dto.request;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;

public record TodayMissionRequest(

	@NotBlank(message = "Content must not be blank")
	@Size(max = 10, message = "Content must be at most 10 characters")
	String content

) {
}
