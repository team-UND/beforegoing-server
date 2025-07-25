package com.und.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.dto.AuthRequest;
import com.und.server.dto.AuthResponse;
import com.und.server.dto.HandshakeRequest;
import com.und.server.dto.HandshakeResponse;
import com.und.server.dto.RefreshTokenRequest;
import com.und.server.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/nonce")
	public ResponseEntity<HandshakeResponse> handshake(@RequestBody @Valid final HandshakeRequest handshakeRequest) {
		final HandshakeResponse handshakeResponse = authService.handshake(handshakeRequest);

		return ResponseEntity.status(HttpStatus.OK).body(handshakeResponse);
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

}
