package com.und.server.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.dto.AuthResponse;
import com.und.server.dto.TestAuthRequest;
import com.und.server.dto.TestHelloResponse;
import com.und.server.entity.Member;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.service.AuthService;
import com.und.server.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/test")
public class TestController {

	private final AuthService authService;
	private final MemberService memberService;

	@PostMapping("/access")
	public ResponseEntity<AuthResponse> requireAccessToken(@RequestBody @Valid final TestAuthRequest request) {
		final AuthResponse response = authService.issueTokensForTest(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/hello")
	public ResponseEntity<TestHelloResponse> greet(final Authentication authentication) {
		if (!(authentication.getPrincipal() instanceof final Long memberId)) {
			throw new ServerException(ServerErrorResult.UNAUTHORIZED_ACCESS);
		}
		final Member member = memberService.findById(memberId)
			.orElseThrow(() -> new ServerException(ServerErrorResult.MEMBER_NOT_FOUND));
		final String nickname = member.getNickname() != null ? member.getNickname() : "Member";
		final TestHelloResponse response = new TestHelloResponse("Hello, " + nickname + "!");

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
