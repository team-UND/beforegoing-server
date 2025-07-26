package com.und.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.entity.RefreshToken;
import com.und.server.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@InjectMocks
	private RefreshTokenService refreshTokenService;

	private final Long memberId = 1L;
	private final String refreshToken = UUID.randomUUID().toString();

	@Test
	@DisplayName("Generates a new UUID-formatted refresh token")
	void Given_Nothing_When_GenerateRefreshToken_Then_ReturnsUuidString() {
		// when
		final String token = refreshTokenService.generateRefreshToken();

		// then
		assertThat(token).isNotNull();
		assertThat(token).hasSize(36); // UUID format
	}

	@Test
	@DisplayName("Saves a refresh token for a member")
	void Given_MemberIdAndToken_When_SaveRefreshToken_Then_RepositorySaveIsCalled() {
		// when
		refreshTokenService.saveRefreshToken(memberId, refreshToken);

		// then
		verify(refreshTokenRepository).save(any(RefreshToken.class));
	}

	@Test
	@DisplayName("Retrieves an existing refresh token for a member")
	void Given_ExistingTokenInRepository_When_GetRefreshToken_Then_ReturnsCorrectToken() {
		// given
		RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.refreshToken(refreshToken)
			.build();

		doReturn(Optional.of(token)).when(refreshTokenRepository).findById(memberId);

		// when
		final String result = refreshTokenService.getRefreshToken(memberId);

		// then
		assertThat(result).isEqualTo(refreshToken);
	}

	@Test
	@DisplayName("Returns null when refresh token is not found")
	void Given_TokenNotInRepository_When_GetRefreshToken_Then_ReturnsNull() {
		// given
		doReturn(Optional.empty()).when(refreshTokenRepository).findById(memberId);

		// when
		final String result = refreshTokenService.getRefreshToken(memberId);

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Deletes a refresh token for a member")
	void Given_MemberId_When_DeleteRefreshToken_Then_RepositoryDeleteIsCalled() {
		// when
		refreshTokenService.deleteRefreshToken(memberId);

		// then
		verify(refreshTokenRepository).deleteById(memberId);
	}

}
