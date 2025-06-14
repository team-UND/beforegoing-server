package com.und.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

import com.und.server.entity.RefreshToken;

@DataRedisTest
public class RefreshTokenRepositoryTest {

	@Autowired
	RefreshTokenRepository refreshTokenRepository;

	@Test
	@DisplayName("Save a refreshToken successfully")
	void saveRefreshTokenSuccessfully() {
		// given
		final RefreshToken token = RefreshToken.builder()
			.memberId(1L)
			.refreshToken("uuid")
			.build();

		// when
		final RefreshToken result = refreshTokenRepository.save(token);

		// then
		assertThat(result.getMemberId()).isEqualTo(1L);
		assertThat(result.getRefreshToken()).isEqualTo("uuid");
	}

	@Test
	@DisplayName("Find a refreshToken by member ID")
	void findRefreshTokenByMemberId() {
		// given
		final RefreshToken token = RefreshToken.builder()
			.memberId(1L)
			.refreshToken("uuid")
			.build();
		refreshTokenRepository.save(token);

		// when
		final Optional<RefreshToken> foundToken = refreshTokenRepository.findById(1L);

		// then
		assertThat(foundToken).isPresent().hasValueSatisfying(result -> {
			assertThat(result.getMemberId()).isEqualTo(1L);
			assertThat(result.getRefreshToken()).isEqualTo("uuid");
		});
	}

}
