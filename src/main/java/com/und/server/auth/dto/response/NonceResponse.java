package com.und.server.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response with a Nonce")
public record NonceResponse(
	@Schema(description = "A unique and single-use string for security", example = "a1b2c3d4-e5f6-78...")
	String nonce
) { }
