package com.und.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to Reissue Tokens")
public record RefreshTokenRequest(
	@Schema(description = "Expired Access Token", example = "eyJhbGciOiJIUzI1Ni...")
	@NotBlank(message = "Access Token must not be blank") String accessToken,

	@Schema(description = "Valid Refresh Token", example = "a1b2c3d4-e5f6-78...")
	@NotBlank(message = "Refresh Token must not be blank") String refreshToken
) { }
