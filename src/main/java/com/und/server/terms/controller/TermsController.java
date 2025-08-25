package com.und.server.terms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.terms.dto.request.EventPushAgreementRequest;
import com.und.server.terms.dto.request.TermsAgreementRequest;
import com.und.server.terms.dto.response.TermsAgreementResponse;
import com.und.server.terms.service.TermsService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/terms")
public class TermsController {

	private final TermsService termsService;

	@GetMapping("")
	public ResponseEntity<TermsAgreementResponse> getTermsAgreement(
		@Parameter(hidden = true) @AuthMember final Long memberId
	) {
		final TermsAgreementResponse termsAgreementResponse = termsService.getTermsAgreement(memberId);

		return ResponseEntity.status(HttpStatus.OK).body(termsAgreementResponse);
	}


	@PostMapping("")
	public ResponseEntity<TermsAgreementResponse> addTermsAgreement(
		@Parameter(hidden = true) @AuthMember final Long memberId,
		@RequestBody @Valid final TermsAgreementRequest termsAgreementRequest
	) {
		final TermsAgreementResponse termsAgreementResponse
			= termsService.addTermsAgreement(memberId, termsAgreementRequest);

		return ResponseEntity.status(HttpStatus.OK).body(termsAgreementResponse);
	}

	@PatchMapping("")
	public ResponseEntity<TermsAgreementResponse> updateEventPushAgreement(
		@Parameter(hidden = true) @AuthMember final Long memberId,
		@RequestBody @Valid final EventPushAgreementRequest eventPushAgreementRequest
	) {
		final TermsAgreementResponse termsAgreementResponse
			= termsService.updateEventPushAgreement(memberId, eventPushAgreementRequest);

		return ResponseEntity.status(HttpStatus.OK).body(termsAgreementResponse);
	}

}
