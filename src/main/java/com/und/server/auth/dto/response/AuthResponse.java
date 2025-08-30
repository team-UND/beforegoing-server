package com.und.server.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication Token Response")
public record AuthResponse(
	@Schema(description = "Token type", example = "Bearer")
	String tokenType,

	@Schema(description = "Access Token for API authentication", example = "eyJhbGciOiJIUzI1Ni...")
	String accessToken,

	@Schema(description = "Access Token expiration time in seconds", example = "3600")
	Integer accessTokenExpiresIn,

	@Schema(description = "Refresh Token for renewing the Access Token", example = "a1b2c3d4-e5f6-78...")
	String refreshToken,

	@Schema(description = "Refresh Token expiration time in seconds", example = "604800")
	Integer refreshTokenExpiresIn
) { }
