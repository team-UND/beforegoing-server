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

	@Transactional
	public void validateRefreshToken(final Long memberId, final String providedToken) {
		validateMemberIdIsNotNull(memberId);
		validateTokenValueIsNotNull(providedToken);

		refreshTokenRepository.findById(memberId)
			.map(RefreshToken::getValue)
			.filter(savedToken -> providedToken.equals(savedToken))
			.orElseThrow(() -> {
				deleteRefreshToken(memberId);
				return new ServerException(ServerErrorResult.INVALID_TOKEN);
			});
	}

	@Transactional
	public void saveRefreshToken(final Long memberId, final String value) {
		validateMemberIdIsNotNull(memberId);
		validateTokenValueIsNotNull(value);

		final RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.value(value)
			.build();

		refreshTokenRepository.save(token);
	}

	@Transactional
	public void deleteRefreshToken(final Long memberId) {
		validateMemberIdIsNotNull(memberId);

		refreshTokenRepository.deleteById(memberId);
	}

	private void validateMemberIdIsNotNull(final Long memberId) {
		if (memberId == null) {
			throw new ServerException(ServerErrorResult.INVALID_MEMBER_ID);
		}
	}

	private void validateTokenValueIsNotNull(final String token) {
		if (token == null) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}
	}

}
