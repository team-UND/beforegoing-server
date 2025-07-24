package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
	@NotNull(message = "Access Token must not be null") @JsonProperty("access_token") String accessToken,
	@NotNull(message = "Refresh Token must not be null") @JsonProperty("refresh_token") String refreshToken
) { }
