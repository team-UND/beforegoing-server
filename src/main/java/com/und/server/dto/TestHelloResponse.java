package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for the test hello endpoint")
public record TestHelloResponse(
	@Schema(description = "Greeting message", example = "Hello, Chori!")
	@JsonProperty("message")
	String message
) { }
