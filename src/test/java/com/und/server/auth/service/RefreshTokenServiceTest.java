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
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.repository.RefreshTokenRepository;
import com.und.server.common.exception.ServerException;
import com.und.server.member.exception.MemberErrorResult;

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
	@DisplayName("Throws an exception when saving a null token")
	void Given_NullToken_When_SaveRefreshToken_Then_ThrowsException() {
		// when
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.saveRefreshToken(memberId, null));

		// then
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Throws an exception when validating with a null token")
	void Given_NullToken_When_VerifyRefreshToken_Then_ThrowsException() {
		// when
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.verifyRefreshToken(memberId, null));

		// then
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Throws an exception when saving a refresh token with a null member ID")
	void Given_NullMemberId_When_SaveRefreshToken_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.saveRefreshToken(null, refreshTokenValue));

		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.INVALID_MEMBER_ID);
	}

	@Test
	@DisplayName("Throws an exception when validating a refresh token with a null member ID")
	void Given_NullMemberId_When_VerifyRefreshToken_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.verifyRefreshToken(null, refreshTokenValue));

		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.INVALID_MEMBER_ID);
	}

	@Test
	@DisplayName("Throws an exception when deleting a refresh token with a null member ID")
	void Given_NullMemberId_When_DeleteRefreshToken_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.deleteRefreshToken(null));

		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.INVALID_MEMBER_ID);
	}

	@Test
	@DisplayName("Succeeds validation if the provided token matches the stored one")
	void Given_MatchingToken_When_VerifyRefreshToken_Then_Succeeds() {
		// given
		final RefreshToken savedToken = RefreshToken.builder()
			.memberId(memberId)
			.value(refreshTokenValue)
			.build();
		doReturn(Optional.of(savedToken)).when(refreshTokenRepository).findById(memberId);

		// when & then
		assertDoesNotThrow(() -> refreshTokenService.verifyRefreshToken(memberId, refreshTokenValue));
	}

	@Test
	@DisplayName("Throws an exception and deletes the token if it does not match")
	void Given_MismatchedToken_When_VerifyRefreshToken_Then_ThrowsExceptionAndDeletes() {
		// given
		final RefreshToken savedToken = RefreshToken.builder()
			.memberId(memberId)
			.value(refreshTokenValue)
			.build();
		doReturn(Optional.of(savedToken)).when(refreshTokenRepository).findById(memberId);

		// when
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.verifyRefreshToken(memberId, "wrong-token"));

		// then
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_TOKEN);
		verify(refreshTokenRepository).deleteById(memberId);
	}

	@Test
	@DisplayName("Throws an exception if the stored token value is null")
	void Given_StoredTokenWithValueNull_When_VerifyRefreshToken_Then_ThrowsException() {
		// given
		final RefreshToken savedTokenWithNullValue = RefreshToken.builder()
			.memberId(memberId)
			.value(null)
			.build();
		doReturn(Optional.of(savedTokenWithNullValue)).when(refreshTokenRepository).findById(memberId);

		// when
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.verifyRefreshToken(memberId, refreshTokenValue));

		// then
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_TOKEN);
		verify(refreshTokenRepository).deleteById(memberId);
	}

	@Test
	@DisplayName("Throws an exception if no token is stored for validation")
	void Given_NoStoredToken_When_VerifyRefreshToken_Then_ThrowsException() {
		// given
		doReturn(Optional.empty()).when(refreshTokenRepository).findById(memberId);

		// when
		final ServerException exception = assertThrows(ServerException.class,
			() -> refreshTokenService.verifyRefreshToken(memberId, refreshTokenValue));

		// then
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_TOKEN);
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
