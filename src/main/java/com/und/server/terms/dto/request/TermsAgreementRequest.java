package com.und.server.terms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Terms Agreement Request DTO")
public record TermsAgreementRequest(
	@Schema(description = "Terms of Service Agreement", example = "true")
	@NotNull(message = "Terms of Service Agreement must not be null")
	@AssertTrue(message = "Terms of Service must be agreed to")
	Boolean termsOfServiceAgreed,

	@Schema(description = "Privacy Policy Agreement", example = "true")
	@NotNull(message = "Privacy Policy Agreement must not be null")
	@AssertTrue(message = "Privacy Policy must be agreed to")
	Boolean privacyPolicyAgreed,

	@Schema(description = "Over 14 Years Old Confirmation", example = "true")
	@NotNull(message = "Over 14 Years Old Confirmation must not be null")
	@AssertTrue(message = "User must be over 14 years old")
	Boolean isOver14,

	@Schema(description = "Event Push Agreement", example = "true")
	@NotNull(message = "Event Push Agreement must not be null")
	Boolean eventPushAgreed
) { }
