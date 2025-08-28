package com.und.server.terms.dto.response;

import com.und.server.terms.entity.Terms;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Terms Agreement Response DTO")
public record TermsAgreementResponse(
	@Schema(description = "Terms Agreement ID", example = "1")
	Long id,

	@Schema(description = "Member ID", example = "1")
	Long memberId,

	@Schema(description = "Terms of Service Agreement", example = "true")
	Boolean termsOfServiceAgreed,

	@Schema(description = "Privacy Policy Agreement", example = "true")
	Boolean privacyPolicyAgreed,

	@Schema(description = "Over 14 Years Old Confirmation", example = "true")
	Boolean isOver14,

	@Schema(description = "Event Push Agreement", example = "true")
	Boolean eventPushAgreed
) {
	public static TermsAgreementResponse from(final Terms terms) {
		return new TermsAgreementResponse(
			terms.getId(),
			terms.getMember().getId(),
			terms.getTermsOfServiceAgreed(),
			terms.getPrivacyPolicyAgreed(),
			terms.getIsOver14(),
			terms.getEventPushAgreed()
		);
	}
}
