package com.und.server.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for the test hello endpoint")
public record TestHelloResponse(
	@Schema(description = "Greeting message", example = "Hello, Chori!")
	String message
) { }
