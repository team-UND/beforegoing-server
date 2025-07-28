package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API Error Response")
public record ErrorResponse(
	@Schema(description = "Error Code", example = "UNAUTHORIZED_ACCESS")
	@JsonProperty("code")
	String code,

	@Schema(description = "Error Message", example = "Unauthorized Access")
	@JsonProperty("message")
	Object message
) { }
