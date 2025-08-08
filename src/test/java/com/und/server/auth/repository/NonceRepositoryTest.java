package com.und.server.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

import com.und.server.auth.entity.Nonce;
import com.und.server.auth.oauth.Provider;

@DataRedisTest
class NonceRepositoryTest {

	@Autowired
	private NonceRepository nonceRepository;

	@Test
	@DisplayName("Saves a Kakao nonce and verifies the returned entity")
	void Given_KakaoNonce_When_Save_Then_ReturnsSavedNonce() {
		// given
		final String nonceValue = UUID.randomUUID().toString();
		final Provider provider = Provider.KAKAO;
		final Nonce nonce = Nonce.builder()
				.value(nonceValue)
				.provider(provider)
				.build();

		// when
		final Nonce savedNonce = nonceRepository.save(nonce);

		// then
		assertThat(savedNonce).isNotNull();
		assertThat(savedNonce.getValue()).isEqualTo(nonceValue);
		assertThat(savedNonce.getProvider()).isEqualTo(provider);
	}

	@Test
	@DisplayName("Saves an Apple nonce and verifies the returned entity")
	void Given_AppleNonce_When_Save_Then_ReturnsSavedNonce() {
		// given
		final String nonceValue = UUID.randomUUID().toString();
		final Provider provider = Provider.APPLE;
		final Nonce nonce = Nonce.builder()
			.value(nonceValue)
			.provider(provider)
			.build();

		// when
		final Nonce savedNonce = nonceRepository.save(nonce);

		// then
		assertThat(savedNonce).isNotNull();
		assertThat(savedNonce.getValue()).isEqualTo(nonceValue);
		assertThat(savedNonce.getProvider()).isEqualTo(provider);
	}

	@Test
	@DisplayName("Finds an existing Kakao nonce by its ID")
	void Given_ExistingKakaoNonce_When_FindById_Then_ReturnsCorrectNonce() {
		// given
		final String nonceValue = UUID.randomUUID().toString();
		final Provider provider = Provider.KAKAO;
		final Nonce nonce = Nonce.builder()
				.value(nonceValue)
				.provider(provider)
				.build();
		nonceRepository.save(nonce);

		// when
		final Optional<Nonce> foundNonceOptional = nonceRepository.findById(nonceValue);

		// then
		assertThat(foundNonceOptional).isPresent().hasValueSatisfying(foundNonce -> {
			assertThat(foundNonce.getValue()).isEqualTo(nonceValue);
			assertThat(foundNonce.getProvider()).isEqualTo(provider);
		});
	}

	@Test
	@DisplayName("Finds an existing Apple nonce by its ID")
	void Given_ExistingAppleNonce_When_FindById_Then_ReturnsCorrectNonce() {
		// given
		final String nonceValue = UUID.randomUUID().toString();
		final Provider provider = Provider.APPLE;
		final Nonce nonce = Nonce.builder()
			.value(nonceValue)
			.provider(provider)
			.build();
		nonceRepository.save(nonce);

		// when
		final Optional<Nonce> foundNonceOptional = nonceRepository.findById(nonceValue);

		// then
		assertThat(foundNonceOptional).isPresent().hasValueSatisfying(foundNonce -> {
			assertThat(foundNonce.getValue()).isEqualTo(nonceValue);
			assertThat(foundNonce.getProvider()).isEqualTo(provider);
		});
	}

	@Test
	@DisplayName("Deletes an existing Kakao nonce successfully")
	void Given_ExistingKakaoNonce_When_DeleteById_Then_NonceIsRemoved() {
		// given
		final String nonceValue = UUID.randomUUID().toString();
		final Provider provider = Provider.KAKAO;
		final Nonce nonce = Nonce.builder()
				.value(nonceValue)
				.provider(provider)
				.build();
		nonceRepository.save(nonce);

		// when
		nonceRepository.deleteById(nonceValue);

		// then
		assertThat(nonceRepository.findById(nonceValue)).isNotPresent();
	}

	@Test
	@DisplayName("Deletes an existing Apple nonce successfully")
	void Given_ExistingAppleNonce_When_DeleteById_Then_NonceIsRemoved() {
		// given
		final String nonceValue = UUID.randomUUID().toString();
		final Provider provider = Provider.APPLE;
		final Nonce nonce = Nonce.builder()
			.value(nonceValue)
			.provider(provider)
			.build();
		nonceRepository.save(nonce);

		// when
		nonceRepository.deleteById(nonceValue);

		// then
		assertThat(nonceRepository.findById(nonceValue)).isNotPresent();
	}

}
