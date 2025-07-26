package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Handshake Response with Nonce")
public record HandshakeResponse(
	@Schema(
		description = "A unique and single-use string for security",
		example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
	)
	@JsonProperty("nonce") String nonce
) { }
