package com.und.server.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API Error Response")
public record ErrorResponse(
	@Schema(description = "Error Code", example = "UNAUTHORIZED_ACCESS")
	String code,

	@Schema(description = "Error Message", example = "Unauthorized Access")
	Object message
) { }
