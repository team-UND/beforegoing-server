package com.und.server.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

import com.und.server.auth.entity.RefreshToken;

@DataRedisTest
class RefreshTokenRepositoryTest {

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Test
	@DisplayName("Saves a refresh token and verifies its properties")
	void Given_RefreshTokenDetails_When_SaveToken_Then_TokenIsPersistedCorrectly() {
		// given
		final Long memberId = 1L;
		final String value = UUID.randomUUID().toString();
		final RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.value(value)
			.build();

		// when
		final RefreshToken result = refreshTokenRepository.save(token);

		// then
		assertThat(result.getMemberId()).isEqualTo(memberId);
		assertThat(result.getValue()).isEqualTo(value);
	}

	@Test
	@DisplayName("Finds a refresh token by its member ID")
	void Given_ExistingRefreshToken_When_FindById_Then_ReturnsCorrectToken() {
		// given
		final Long memberId = 1L;
		final String value = UUID.randomUUID().toString();
		final RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.value(value)
			.build();
		refreshTokenRepository.save(token);

		// when
		final Optional<RefreshToken> foundToken = refreshTokenRepository.findById(memberId);

		// then
		assertThat(foundToken).isPresent().hasValueSatisfying(result -> {
			assertThat(result.getMemberId()).isEqualTo(memberId);
			assertThat(result.getValue()).isEqualTo(value);
		});
	}

	@Test
	@DisplayName("Deletes an existing refresh token by its ID")
	void Given_ExistingRefreshToken_When_DeleteById_Then_TokenIsRemoved() {
		// given
		final Long memberId = 1L;
		final String value = UUID.randomUUID().toString();
		final RefreshToken token = RefreshToken.builder()
			.memberId(memberId)
			.value(value)
			.build();
		final RefreshToken savedToken = refreshTokenRepository.save(token);

		// when
		refreshTokenRepository.deleteById(savedToken.getMemberId());

		// then
		assertThat(refreshTokenRepository.findById(savedToken.getMemberId())).isNotPresent();
	}

}
