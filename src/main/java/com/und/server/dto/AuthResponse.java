package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
	@JsonProperty("token_type") String tokenType,
	@JsonProperty("access_token") String accessToken,
	@JsonProperty("access_token_expires_in") Integer accessTokenExpiresIn,
	@JsonProperty("refresh_token") String refreshToken,
	@JsonProperty("refresh_token_expires_in") Integer refreshTokenExpiresIn
) { }
