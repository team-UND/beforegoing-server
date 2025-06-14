package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
	@NotNull @JsonProperty("access_token") String accessToken,
	@NotNull @JsonProperty("refresh_token") String refreshToken
) { }
