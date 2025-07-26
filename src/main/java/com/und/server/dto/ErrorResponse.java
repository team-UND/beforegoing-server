package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API Error Response")
public record ErrorResponse(
	@Schema(description = "Error Code", example = "INVALID_PARAMETER")
	@JsonProperty("code")
	String code,

	@Schema(description = "Error Message", example = "Invalid Parameter")
	@JsonProperty("message")
	Object message
) { }
