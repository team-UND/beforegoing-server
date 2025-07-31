package com.und.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for Authentication with ID Token")
public record AuthRequest(
	@Schema(description = "OAuth provider name", example = "kakao")
	@NotNull(message = "Provider must not be null") @JsonProperty("provider") String provider,

	@Schema(description = "ID Token from the OAuth provider", example = "eyJhbGciOiJIUzI1Ni...")
	@NotNull(message = "ID Token must not be null") @JsonProperty("id_token") String idToken
) { }
