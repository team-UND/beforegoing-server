package com.und.server.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.auth.entity.Nonce;
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.oauth.Provider;
import com.und.server.auth.repository.NonceRepository;
import com.und.server.common.exception.ServerException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonceService {

	private final NonceRepository nonceRepository;

	public String generateNonceValue() {
		return UUID.randomUUID().toString();
	}

	@Transactional
	public void verifyNonce(final String value, final Provider provider) {
		validateNonceValue(value);
		validateProvider(provider);

		nonceRepository.findById(value)
			.filter(n -> n.getProvider() == provider)
			.orElseThrow(() -> new ServerException(AuthErrorResult.INVALID_NONCE));

		nonceRepository.deleteById(value);
	}

	@Transactional
	public void saveNonce(final String value, final Provider provider) {
		validateNonceValue(value);
		validateProvider(provider);

		final Nonce nonce = Nonce.builder()
			.value(value)
			.provider(provider)
			.build();

		nonceRepository.save(nonce);
	}

	private void validateNonceValue(final String value) {
		if (value == null) {
			throw new ServerException(AuthErrorResult.INVALID_NONCE);
		}
	}

	private void validateProvider(final Provider provider) {
		if (provider == null) {
			throw new ServerException(AuthErrorResult.INVALID_PROVIDER);
		}
	}

}
