package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record HandshakeRequest(
	@NotNull(message = "Provider must not be null") @JsonProperty ("provider") String provider
) { }
