package com.und.server.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for issuing test tokens")
public record TestAuthRequest(
	@Schema(description = "OAuth provider name", example = "kakao")
	@NotBlank(message = "Provider name must not be blank")
	String provider,

	@Schema(description = "Unique ID from the provider", example = "123456789")
	@NotBlank(message = "Provider ID must not be blank")
	String providerId
) { }
