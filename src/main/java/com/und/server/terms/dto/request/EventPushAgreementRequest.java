package com.und.server.terms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EventPushAgreementRequest(
	@Schema(description = "Event Push Agreement", example = "false")
	@NotNull(message = "Event Push Agreement must not be null")
	Boolean eventPushAgreed
) { }
