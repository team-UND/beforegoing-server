package com.und.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.dto.AuthResponse;
import com.und.server.dto.TestAuthRequest;
import com.und.server.dto.TestHelloResponse;
import com.und.server.entity.Member;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.oauth.Provider;
import com.und.server.repository.MemberRepository;
import com.und.server.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {

	private final AuthService authService;
	private final MemberRepository memberRepository;

	@PostMapping("/api/v1/auth/access")
	public ResponseEntity<AuthResponse> requireAccessToken(@RequestBody TestAuthRequest request) {

		final Provider provider = request.provider();
		final String providerId = request.providerId();
		final String nickname = request.nickname();

		Member member = authService.findMemberByProviderId(provider, providerId);
		if (member == null) {
			member = authService.createMember(provider, providerId, nickname);
		}
		final AuthResponse response = authService.issueTokens(member.getId());

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/hello")
	public ResponseEntity<TestHelloResponse> greet(Authentication authentication) {
		final Long memberId = (Long) authentication.getPrincipal();
		final Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ServerException(ServerErrorResult.MEMBER_NOT_FOUND));
		final String nickname = member.getNickname() != null ? member.getNickname() : "Member";
		final TestHelloResponse response = new TestHelloResponse("Hello, " + nickname + "!");

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
