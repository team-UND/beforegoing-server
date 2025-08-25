package com.und.server.common.controller;


import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.dto.response.AuthResponse;
import com.und.server.auth.filter.AuthMember;
import com.und.server.auth.service.AuthService;
import com.und.server.common.dto.request.TestAuthRequest;
import com.und.server.common.dto.response.TestHelloResponse;
import com.und.server.member.dto.response.MemberResponse;
import com.und.server.member.entity.Member;
import com.und.server.member.service.MemberService;
import com.und.server.terms.dto.response.TermsAgreementResponse;
import com.und.server.terms.service.TermsService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// This controller is for testing/development and is disabled in prod/stg via @Profile.
@Profile("!prod & !stg")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/test")
public class TestController {

	private final AuthService authService;
	private final MemberService memberService;
	private final TermsService termsService;

	@PostMapping("/access")
	public ResponseEntity<AuthResponse> requireAccessToken(@RequestBody @Valid final TestAuthRequest request) {
		final AuthResponse response = authService.issueTokensForTest(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/hello")
	public ResponseEntity<TestHelloResponse> greet(@Parameter(hidden = true) @AuthMember final Long memberId) {
		final Member member = memberService.findMemberById(memberId);
		final String nickname = member.getNickname();
		final TestHelloResponse response = new TestHelloResponse("Hello, " + nickname + "!");

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/members")
	public ResponseEntity<List<MemberResponse>> getMemberList() {
		final List<MemberResponse> members = memberService.getMemberList();

		return ResponseEntity.status(HttpStatus.OK).body(members);
	}

	@GetMapping("/terms")
	public ResponseEntity<List<TermsAgreementResponse>> getTermsList() {
		final List<TermsAgreementResponse> terms = termsService.getTermsList();

		return ResponseEntity.status(HttpStatus.OK).body(terms);
	}

}
