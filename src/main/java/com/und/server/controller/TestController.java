package com.und.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.dto.TestResponse;
import com.und.server.entity.Member;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {

	private final MemberRepository memberRepository;

	@GetMapping("/hello")
	public ResponseEntity<TestResponse> greet(Authentication authentication) {
		final Long memberId = (Long) authentication.getPrincipal();
		final Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ServerException(ServerErrorResult.MEMBER_NOT_FOUND));
		final String nickname = member.getNickname() != null ? member.getNickname() : "Member";
		final TestResponse testResponse = new TestResponse("Hello, " + nickname + "!");

		return ResponseEntity.status(HttpStatus.OK).body(testResponse);
	}

}
