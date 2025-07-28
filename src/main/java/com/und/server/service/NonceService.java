package com.und.server.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.und.server.entity.Nonce;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.oauth.Provider;
import com.und.server.repository.NonceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NonceService {

	private final NonceRepository nonceRepository;

	public String generateNonceValue() {
		return UUID.randomUUID().toString();
	}

	public void validateNonce(final String nonceValue, final Provider provider) {
		nonceRepository.findById(nonceValue)
			.filter(n -> n.getProvider() == provider)
			.orElseThrow(() -> new ServerException(ServerErrorResult.INVALID_NONCE));

		nonceRepository.deleteById(nonceValue);
	}


	public void saveNonce(final String value, final Provider provider) {
		final Nonce nonce = Nonce.builder()
			.value(value)
			.provider(provider)
			.build();

		nonceRepository.save(nonce);
	}

	public void deleteNonce(final String value) {
		nonceRepository.deleteById(value);
	}

}
