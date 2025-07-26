package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for issuing test tokens")
public record TestAuthRequest(
	@Schema(description = "OAuth provider name", example = "kakao")
	@NotNull(message = "Provider must not be null") @JsonProperty("provider")
	String provider,

	@Schema(description = "Unique ID from the provider", example = "123456789")
	@NotNull(message = "Provider ID must not be null") @JsonProperty("provider_id")
	String providerId,

	@Schema(description = "User's nickname", example = "Chori")
	@NotNull(message = "Nickname must not be null") @JsonProperty("nickname")
	String nickname
) { }
