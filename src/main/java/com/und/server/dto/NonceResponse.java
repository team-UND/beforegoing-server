package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response with a Nonce")
public record NonceResponse(
	@Schema(description = "A unique and single-use string for security", example = "a1b2c3d4-e5f6-78...")
	@JsonProperty("nonce") String nonce
) { }
