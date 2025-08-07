package com.und.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.auth.entity.Nonce;
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.oauth.Provider;
import com.und.server.auth.repository.NonceRepository;
import com.und.server.common.exception.ServerException;

@ExtendWith(MockitoExtension.class)
class NonceServiceTest {

	@InjectMocks
	private NonceService nonceService;

	@Mock
	private NonceRepository nonceRepository;

	private final String nonceValue = "test-nonce";

	@Test
	@DisplayName("Generates a new nonce in UUID format")
	void Given_Nothing_When_GenerateNonceValue_Then_ReturnsUuid() {
		// when
		final String generatedNonce = nonceService.generateNonceValue();

		// then
		assertThat(generatedNonce).isNotNull();
		assertDoesNotThrow(() -> UUID.fromString(generatedNonce));
	}

	@Test
	@DisplayName("Throws an exception when validating with a null nonce value")
	void Given_NullNonceValue_When_ValidateNonce_Then_ThrowsException() {
		// when & then
		final Provider provider = Provider.KAKAO;
		final ServerException exception = assertThrows(ServerException.class,
			() -> nonceService.validateNonce(null, provider));
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_NONCE);
	}

	@Test
	@DisplayName("Throws an exception when validating with a null provider")
	void Given_NullProvider_When_ValidateNonce_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> nonceService.validateNonce(nonceValue, null));
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Throws an exception when saving with a null nonce value")
	void Given_NullNonceValue_When_SaveNonce_Then_ThrowsException() {
		// when & then
		final Provider provider = Provider.KAKAO;
		final ServerException exception = assertThrows(ServerException.class,
			() -> nonceService.saveNonce(null, provider));
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_NONCE);
	}

	@Test
	@DisplayName("Throws an exception when saving with a null provider")
	void Given_NullProvider_When_SaveNonce_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> nonceService.saveNonce(nonceValue, null));
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Succeeds validation for a valid Kakao nonce")
	void Given_ValidKakaoNonce_When_ValidateNonce_Then_SucceedsAndDeletesNonce() {
		// given
		final Provider provider = Provider.KAKAO;
		final Nonce savedNonce = Nonce.builder().value(nonceValue).provider(provider).build();
		doReturn(Optional.of(savedNonce)).when(nonceRepository).findById(nonceValue);

		// when & then
		assertDoesNotThrow(() -> nonceService.validateNonce(nonceValue, provider));
		verify(nonceRepository).deleteById(nonceValue);
	}

	@Test
	@DisplayName("Succeeds validation for a valid Apple nonce")
	void Given_ValidAppleNonce_When_ValidateNonce_Then_SucceedsAndDeletesNonce() {
		// given
		final Provider provider = Provider.APPLE;
		final Nonce savedNonce = Nonce.builder().value(nonceValue).provider(provider).build();
		doReturn(Optional.of(savedNonce)).when(nonceRepository).findById(nonceValue);

		// when & then
		assertDoesNotThrow(() -> nonceService.validateNonce(nonceValue, provider));
		verify(nonceRepository).deleteById(nonceValue);
	}

	@Test
	@DisplayName("Throws an exception for a non-existent nonce")
	void Given_NonExistentNonce_When_ValidateNonce_Then_ThrowsException() {
		// given
		doReturn(Optional.empty()).when(nonceRepository).findById(nonceValue);
		final Provider provider = Provider.KAKAO;

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> nonceService.validateNonce(nonceValue, provider));
		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_NONCE);
	}

	@Test
	@DisplayName("Throws an exception for a nonce with a mismatched provider")
	void Given_MismatchedProvider_When_ValidateNonce_Then_ThrowsException() {
		// given
		// Nonce is saved with KAKAO provider
		final Nonce savedNonce = Nonce.builder().value(nonceValue).provider(Provider.KAKAO).build();
		doReturn(Optional.of(savedNonce)).when(nonceRepository).findById(nonceValue);

		// but validation is attempted with a different provider
		final Provider differentProvider = Provider.APPLE;

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> nonceService.validateNonce(nonceValue, differentProvider));

		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_NONCE);
		// The nonce should not be deleted if the provider does not match
		verify(nonceRepository, never()).deleteById(nonceValue);
	}

	@Test
	@DisplayName("Saves a Kakao nonce successfully")
	void Given_KakaoNonce_When_SaveNonce_Then_SavesToRepository() {
		// when
		final Provider provider = Provider.KAKAO;
		nonceService.saveNonce(nonceValue, provider);

		// then
		verify(nonceRepository).save(any(Nonce.class));
	}

	@Test
	@DisplayName("Saves an Apple nonce successfully")
	void Given_AppleNonce_When_SaveNonce_Then_SavesToRepository() {
		// when
		final Provider provider = Provider.APPLE;
		nonceService.saveNonce(nonceValue, provider);

		// then
		verify(nonceRepository).save(any(Nonce.class));
	}

}
