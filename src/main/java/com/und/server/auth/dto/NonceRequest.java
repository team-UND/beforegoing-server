package com.und.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for issuing a Nonce")
public record NonceRequest(
	@Schema(description = "OAuth provider name", example = "kakao")
	@NotNull(message = "Provider must not be null") @JsonProperty("provider") String provider
) { }
