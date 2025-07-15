package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record AuthRequest(
	@NotNull(message = "Provider must not be null") @JsonProperty("provider") String provider,
	@NotNull(message = "ID Token must not be null") @JsonProperty("id_token") String idToken
) { }
