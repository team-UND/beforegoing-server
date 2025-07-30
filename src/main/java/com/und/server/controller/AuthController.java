package com.und.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.dto.AuthRequest;
import com.und.server.dto.AuthResponse;
import com.und.server.dto.NonceRequest;
import com.und.server.dto.NonceResponse;
import com.und.server.dto.RefreshTokenRequest;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.service.AuthService;

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
	public ResponseEntity<Void> logout(final Authentication authentication) {
		if (!(authentication.getPrincipal() instanceof final Long memberId)) {
			throw new ServerException(ServerErrorResult.UNAUTHORIZED_ACCESS);
		}
		authService.logout(memberId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
