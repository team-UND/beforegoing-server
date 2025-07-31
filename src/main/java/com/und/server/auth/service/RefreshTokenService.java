package com.und.server.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.auth.entity.RefreshToken;
import com.und.server.auth.repository.RefreshTokenRepository;
import com.und.server.common.exception.ServerErrorResult;
import com.und.server.common.exception.ServerException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	public String generateRefreshToken() {
		return UUID.randomUUID().toString();
	}

	public String getRefreshToken(final Long memberId) {
		return refreshTokenRepository.findById(memberId)
			.map(RefreshToken::getValue)
			.orElse(null);
	}

	@Transactional
	public void validateRefreshToken(final Long memberId, final String providedToken) {
		refreshTokenRepository.findById(memberId)
			.map(RefreshToken::getValue)
			.filter(savedToken -> savedToken.equals(providedToken))
			.orElseThrow(() -> {
				deleteRefreshToken(memberId);
				return new ServerException(ServerErrorResult.INVALID_TOKEN);
			});
	}

	@Transactional
	public void saveRefreshToken(final Long memberId, final String refreshToken) {
		final RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.value(refreshToken)
			.build();

		refreshTokenRepository.save(token);
	}

	@Transactional
	public void deleteRefreshToken(final Long memberId) {
		refreshTokenRepository.deleteById(memberId);
	}
}
