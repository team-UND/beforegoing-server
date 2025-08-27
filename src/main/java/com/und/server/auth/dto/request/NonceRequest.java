package com.und.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for issuing a Nonce")
public record NonceRequest(
	@Schema(description = "OAuth provider name", example = "kakao")
	@NotBlank(message = "Provider name must not be blank") String provider
) { }
