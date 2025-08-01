package com.und.server.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.member.service.MemberService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class MemberController {

	private final MemberService memberService;

	@DeleteMapping("/member")
	public ResponseEntity<Void> deleteMember(@Parameter(hidden = true) @AuthMember final Long memberId) {
		memberService.deleteMemberById(memberId);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
