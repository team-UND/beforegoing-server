package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication Token Response")
public record AuthResponse(
	@Schema(description = "Token type", example = "Bearer")
	@JsonProperty("token_type") String tokenType,

	@Schema(description = "Access Token for API authentication", example = "eyJhbGciOiJIUzI1Ni...")
	@JsonProperty("access_token") String accessToken,

	@Schema(description = "Access Token expiration time in seconds", example = "3600")
	@JsonProperty("access_token_expires_in") Integer accessTokenExpiresIn,

	@Schema(description = "Refresh Token for renewing the Access Token", example = "eyJhbGciOiJIUzI1Ni...")
	@JsonProperty("refresh_token") String refreshToken,

	@Schema(description = "Refresh Token expiration time in seconds", example = "604800")
	@JsonProperty("refresh_token_expires_in") Integer refreshTokenExpiresIn
) { }
