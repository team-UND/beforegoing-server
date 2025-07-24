package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record TestAuthRequest(
	@NotNull(message = "Provider must not be null") @JsonProperty("provider") String provider,
	@NotNull(message = "Provider ID must not be null") @JsonProperty("provider_id") String providerId,
	@NotNull(message = "Nickname must not be null") @JsonProperty("nickname") String nickname
) { }
