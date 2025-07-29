package com.und.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

import com.und.server.entity.RefreshToken;

@DataRedisTest
class RefreshTokenRepositoryTest {

	@Autowired
	RefreshTokenRepository refreshTokenRepository;

	@Test
	@DisplayName("Saves a refresh token and verifies its properties")
	void Given_RefreshTokenDetails_When_SaveToken_Then_TokenIsPersistedCorrectly() {
		// given
		final RefreshToken token = RefreshToken.builder()
			.memberId(1L)
			.value("uuid")
			.build();

		// when
		final RefreshToken result = refreshTokenRepository.save(token);

		// then
		assertThat(result.getMemberId()).isEqualTo(1L);
		assertThat(result.getValue()).isEqualTo("uuid");
	}

	@Test
	@DisplayName("Finds a refresh token by its member ID")
	void Given_ExistingRefreshToken_When_FindById_Then_ReturnsCorrectToken() {
		// given
		final RefreshToken token = RefreshToken.builder()
			.memberId(1L)
			.value("uuid")
			.build();
		refreshTokenRepository.save(token);

		// when
		final Optional<RefreshToken> foundToken = refreshTokenRepository.findById(1L);

		// then
		assertThat(foundToken).isPresent().hasValueSatisfying(result -> {
			assertThat(result.getMemberId()).isEqualTo(1L);
			assertThat(result.getValue()).isEqualTo("uuid");
		});
	}

}
