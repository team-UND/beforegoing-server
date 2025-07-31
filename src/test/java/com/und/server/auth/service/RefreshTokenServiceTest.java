package com.und.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.auth.entity.RefreshToken;
import com.und.server.auth.repository.RefreshTokenRepository;
import com.und.server.common.exception.ServerErrorResult;
import com.und.server.common.exception.ServerException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@InjectMocks
	private RefreshTokenService refreshTokenService;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	private final Long memberId = 1L;
	private final String refreshTokenValue = "test-refresh-token";

	@Test
	@DisplayName("Generates a new refresh token in UUID format")
	void Given_Nothing_When_GenerateRefreshToken_Then_ReturnsUuid() {
		// when
		final String generatedToken = refreshTokenService.generateRefreshToken();

		// then
		assertThat(generatedToken).isNotNull();
		assertDoesNotThrow(() -> UUID.fromString(generatedToken));
	}

	@Test
	@DisplayName("Returns the token value if a refresh token is stored")
	void Given_StoredToken_When_GetRefreshToken_Then_ReturnsTokenValue() {
		// given
		final RefreshToken savedToken = RefreshToken.builder()
			.memberId(memberId)
			.value(refreshTokenValue)
			.build();
		doReturn(Optional.of(savedToken)).when(refreshTokenRepository).findById(memberId);

		// when
		final String foundToken = refreshTokenService.getRefreshToken(memberId);

		// then
		assertThat(foundToken).isEqualTo(refreshTokenValue);
	}

	@Test
	@DisplayName("Returns null if no refresh token is stored")
	void Given_NoToken_When_GetRefreshToken_Then_ReturnsNull() {
		// given
		doReturn(Optional.empty()).when(refreshTokenRepository).findById(memberId);

		// when
		final String foundToken = refreshTokenService.getRefreshToken(memberId);

		// then
		assertThat(foundToken).isNull();
	}

	@Test
	@DisplayName("Saves a refresh token to the repository")
	void Given_MemberIdAndToken_When_SaveRefreshToken_Then_CallsRepositorySave() {
		// given
		final ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

		// when
		refreshTokenService.saveRefreshToken(memberId, refreshTokenValue);

		// then
		verify(refreshTokenRepository).save(captor.capture());
		final RefreshToken capturedToken = captor.getValue();

		assertThat(capturedToken.getMemberId()).isEqualTo(memberId);
		assertThat(capturedToken.getValue()).isEqualTo(refreshTokenValue);
	}

	@Test
	@DisplayName("Succeeds validation if the provided token matches the stored one")
	void Given_MatchingToken_When_ValidateRefreshToken_Then_Succeeds() {
		// given
		final RefreshToken savedToken = RefreshToken.builder()
			.memberId(memberId)
			.value(refreshTokenValue)
			.build();
		doReturn(Optional.of(savedToken)).when(refreshTokenRepository).findById(memberId);

		// when & then
		assertDoesNotThrow(() -> refreshTokenService.validateRefreshToken(memberId, refreshTokenValue));
	}

	@Test
	@DisplayName("Throws an exception and deletes the token if it does not match")
	void Given_MismatchedToken_When_ValidateRefreshToken_Then_ThrowsExceptionAndDeletes() {
		// given
		final RefreshToken savedToken = RefreshToken.builder()
			.memberId(memberId)
			.value(refreshTokenValue)
			.build();
		doReturn(Optional.of(savedToken)).when(refreshTokenRepository).findById(memberId);

		// when
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.validateRefreshToken(memberId, "wrong-token"));

		// then
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_TOKEN);
		verify(refreshTokenRepository).deleteById(memberId);
	}

	@Test
	@DisplayName("Throws an exception if no token is stored for validation")
	void Given_NoStoredToken_When_ValidateRefreshToken_Then_ThrowsException() {
		// given
		doReturn(Optional.empty()).when(refreshTokenRepository).findById(memberId);

		// when
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.validateRefreshToken(memberId, refreshTokenValue));

		// then
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_TOKEN);
		verify(refreshTokenRepository).deleteById(memberId);
	}

	@Test
	@DisplayName("Deletes a refresh token")
	void Given_MemberId_When_DeleteRefreshToken_Then_CallsRepositoryDelete() {
		// when
		refreshTokenService.deleteRefreshToken(memberId);

		// then
		verify(refreshTokenRepository).deleteById(memberId);
	}
}
