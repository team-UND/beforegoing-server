package com.und.server.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.member.dto.request.NicknameRequest;
import com.und.server.member.dto.response.MemberResponse;
import com.und.server.member.service.MemberService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class MemberController {

	private final MemberService memberService;

	@PatchMapping("/member/nickname")
	public ResponseEntity<MemberResponse> updateNickname(
		@Parameter(hidden = true) @AuthMember final Long memberId,
		@RequestBody @Valid final NicknameRequest nicknameRequest
	) {
		final MemberResponse memberResponse = memberService.updateNickname(memberId, nicknameRequest);

		return ResponseEntity.status(HttpStatus.OK).body(memberResponse);
	}

	@DeleteMapping("/member")
	@ApiResponse(responseCode = "204", description = "Delete member successful")
	public ResponseEntity<Void> deleteMember(@Parameter(hidden = true) @AuthMember final Long memberId) {
		memberService.deleteMemberById(memberId);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
