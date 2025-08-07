package com.und.server.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.dto.AuthRequest;
import com.und.server.auth.dto.AuthResponse;
import com.und.server.auth.dto.NonceRequest;
import com.und.server.auth.dto.NonceResponse;
import com.und.server.auth.dto.RefreshTokenRequest;
import com.und.server.auth.filter.AuthMember;
import com.und.server.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/nonce")
	public ResponseEntity<NonceResponse> handshake(@RequestBody @Valid final NonceRequest nonceRequest) {
		final NonceResponse nonceResponse = authService.handshake(nonceRequest);

		return ResponseEntity.status(HttpStatus.OK).body(nonceResponse);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid final AuthRequest authRequest) {
		final AuthResponse authResponse = authService.login(authRequest);

		return ResponseEntity.status(HttpStatus.OK).body(authResponse);
	}

	@PostMapping("/tokens")
	public ResponseEntity<AuthResponse> reissueTokens(
		@RequestBody @Valid final RefreshTokenRequest refreshTokenRequest
	) {
		final AuthResponse authResponse = authService.reissueTokens(refreshTokenRequest);

		return ResponseEntity.status(HttpStatus.OK).body(authResponse);
	}

	@DeleteMapping("/logout")
	@ApiResponse(responseCode = "204", description = "Logout successful")
	public ResponseEntity<Void> logout(@Parameter(hidden = true) @AuthMember final Long memberId) {
		authService.logout(memberId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
