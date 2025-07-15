package com.und.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
	@DisplayName("Generate UUID nonce value")
	void generateNonceValue() {
		// when
		String nonce = nonceService.generateNonceValue();

		// then
		assertThat(nonce).isNotNull();
		assertThat(nonce).hasSize(36); // UUID format
	}

	@Test
	@DisplayName("Save nonce successfully")
	void saveNonceSuccessfully() {
		// given
		String nonceValue = UUID.randomUUID().toString();
		Provider provider = Provider.KAKAO;

		// when
		nonceService.saveNonce(nonceValue, provider);

		// then
		verify(nonceRepository).save(any(Nonce.class));
	}

	@Test
	@DisplayName("Validate nonce successfully")
	void validateNonceSuccessfully() {
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
	@DisplayName("Throw exception when nonce not found")
	void throwExceptionWhenNonceNotFound() {
		// given
		String nonceValue = UUID.randomUUID().toString();
		Provider provider = Provider.KAKAO;

		doReturn(Optional.empty()).when(nonceRepository).findById(nonceValue);

		// when & then
		ServerException exception = assertThrows(ServerException.class, () -> {
			nonceService.validateNonce(nonceValue, provider);
		});
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_NONCE);
		verify(nonceRepository, never()).deleteById(anyString());
	}

	@Test
	@DisplayName("Throw exception when provider does not match")
	void throwExceptionWhenProviderDoesNotMatch() {
		// given
		String nonceValue = UUID.randomUUID().toString();
		Provider requestProvider = Provider.KAKAO;
		Nonce storedNonce = Nonce.builder()
			.value(nonceValue)
			.provider(null) // Stored nonce has a different (null) provider
			.build();

		doReturn(Optional.of(storedNonce)).when(nonceRepository).findById(nonceValue);

		// when & then
		ServerException exception = assertThrows(ServerException.class,
			() -> nonceService.validateNonce(nonceValue, requestProvider));
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_NONCE);
		verify(nonceRepository, never()).deleteById(anyString());
	}

	@Test
	@DisplayName("Delete nonce successfully")
	void deleteNonceSuccessfully() {
		// given
		String nonceValue = UUID.randomUUID().toString();

		// when
		nonceService.deleteNonce(nonceValue);

		// then
		verify(nonceRepository).deleteById(nonceValue);
	}

}
