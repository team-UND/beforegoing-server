package com.und.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for Authentication with ID Token")
public record AuthRequest(
	@Schema(description = "OAuth provider name", example = "kakao")
	@NotBlank(message = "Provider name must not be blank") @JsonProperty("provider") String provider,

	@Schema(description = "ID Token from the OAuth provider", example = "eyJhbGciOiJIUzI1Ni...")
	@NotBlank(message = "ID Token must not be blank") @JsonProperty("id_token") String idToken
) { }
