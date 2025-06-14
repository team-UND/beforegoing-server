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
	@DisplayName("Generate a new refresh token")
	void generateRefreshToken() {
		// when
		String token = refreshTokenService.generateRefreshToken();

		// then
		assertThat(token).isNotNull();
		assertThat(token).isInstanceOf(String.class);
	}

	@Test
	@DisplayName("Save refresh token for member")
	void saveRefreshToken() {
		// when
		refreshTokenService.saveRefreshToken(memberId, refreshToken);

		// then
		verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
	}

	@Test
	@DisplayName("Get existing refresh token for member")
	void getExistingRefreshToken() {
		// given
		RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.refreshToken(refreshToken)
			.build();

		when(refreshTokenRepository.findById(memberId)).thenReturn(Optional.of(token));

		// when
		String result = refreshTokenService.getRefreshToken(memberId);

		// then
		assertThat(result).isEqualTo(refreshToken);
	}

	@Test
	@DisplayName("Return null if refresh token does not exist")
	void getRefreshTokenReturnsNullIfNotFound() {
		// given
		when(refreshTokenRepository.findById(memberId)).thenReturn(Optional.empty());

		// when
		String result = refreshTokenService.getRefreshToken(memberId);

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Delete refresh token for member")
	void deleteRefreshToken() {
		// when
		refreshTokenService.deleteRefreshToken(memberId);

		// then
		verify(refreshTokenRepository, times(1)).deleteById(memberId);
	}
}
