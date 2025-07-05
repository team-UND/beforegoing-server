package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TestHelloResponse(
	@JsonProperty("message") String message
) { }
