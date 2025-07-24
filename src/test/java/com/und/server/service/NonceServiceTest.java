package com.und.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.entity.Nonce;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.oauth.Provider;
import com.und.server.repository.NonceRepository;

@ExtendWith(MockitoExtension.class)
class NonceServiceTest {

	@Mock
	private NonceRepository nonceRepository;

	@InjectMocks
	private NonceService nonceService;

	@Test
	@DisplayName("Generates a UUID-formatted nonce value")
	void Given_Nothing_When_GenerateNonceValue_Then_ReturnsUuidString() {
		// when
		String nonce = nonceService.generateNonceValue();

		// then
		assertThat(nonce).isNotNull();
		assertThat(nonce).hasSize(36); // UUID format
	}

	@Test
	@DisplayName("Saves a nonce successfully")
	void Given_NonceValueAndProvider_When_SaveNonce_Then_RepositorySaveIsCalled() {
		// given
		String nonceValue = UUID.randomUUID().toString();
		Provider provider = Provider.KAKAO;

		// when
		nonceService.saveNonce(nonceValue, provider);

		// then
		verify(nonceRepository).save(any(Nonce.class));
	}

	@Test
	@DisplayName("Throws an exception when validating a non-existent nonce")
	void Given_NonceNotInRepository_When_ValidateNonce_Then_ThrowsInvalidNonceException() {
		// given
		String nonceValue = UUID.randomUUID().toString();
		Provider provider = Provider.KAKAO;

		doReturn(Optional.empty()).when(nonceRepository).findById(nonceValue);

		// when & then
		assertThatThrownBy(() -> nonceService.validateNonce(nonceValue, provider))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_NONCE);
		verify(nonceRepository, never()).deleteById(anyString());
	}

	@Test
	@DisplayName("Throws an exception when the provider does not match")
	void Given_NonceWithMismatchedProvider_When_ValidateNonce_Then_ThrowsInvalidNonceException() {
		// given
		String nonceValue = UUID.randomUUID().toString();
		Provider requestProvider = Provider.KAKAO;
		Nonce storedNonce = Nonce.builder()
			.value(nonceValue)
			.provider(null) // Stored nonce has a different (null) provider
			.build();

		doReturn(Optional.of(storedNonce)).when(nonceRepository).findById(nonceValue);

		// when & then
		assertThatThrownBy(() -> nonceService.validateNonce(nonceValue, requestProvider))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_NONCE);
		verify(nonceRepository, never()).deleteById(anyString());
	}

	@Test
	@DisplayName("Validates a nonce successfully and deletes it")
	void Given_ValidNonceInRepository_When_ValidateNonce_Then_DeletesNonce() {
		// given
		String nonceValue = UUID.randomUUID().toString();
		Provider provider = Provider.KAKAO;
		Nonce nonce = Nonce.builder()
			.value(nonceValue)
			.provider(provider)
			.build();

		doReturn(Optional.of(nonce)).when(nonceRepository).findById(nonceValue);

		// when
		nonceService.validateNonce(nonceValue, provider);

		// then
		verify(nonceRepository).deleteById(nonceValue);
	}

	@Test
	@DisplayName("Deletes a nonce successfully")
	void Given_NonceValue_When_DeleteNonce_Then_RepositoryDeleteIsCalled() {
		// given
		String nonceValue = UUID.randomUUID().toString();

		// when
		nonceService.deleteNonce(nonceValue);

		// then
		verify(nonceRepository).deleteById(nonceValue);
	}

}
