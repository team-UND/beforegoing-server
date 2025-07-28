package com.und.server.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.und.server.entity.RefreshToken;
import com.und.server.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	public String generateRefreshToken() {
		return UUID.randomUUID().toString();
	}

	public String getRefreshToken(final Long memberId) {
		return refreshTokenRepository.findById(memberId)
			.map(RefreshToken::getRefreshToken)
			.orElse(null);
	}

	public void saveRefreshToken(final Long memberId, final String refreshToken) {
		final RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.refreshToken(refreshToken)
			.build();

		refreshTokenRepository.save(token);
	}

	public void deleteRefreshToken(final Long memberId) {
		refreshTokenRepository.deleteById(memberId);
	}
}
