package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TestResponse(
	@JsonProperty("message") String message
) { }
