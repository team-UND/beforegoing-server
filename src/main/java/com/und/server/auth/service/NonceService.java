package com.und.server.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.auth.entity.Nonce;
import com.und.server.auth.oauth.Provider;
import com.und.server.auth.repository.NonceRepository;
import com.und.server.common.exception.ServerErrorResult;
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
	public void validateNonce(final String value, final Provider provider) {
		nonceRepository.findById(value)
			.filter(n -> n.getProvider() == provider)
			.orElseThrow(() -> new ServerException(ServerErrorResult.INVALID_NONCE));

		nonceRepository.deleteById(value);
	}


	@Transactional
	public void saveNonce(final String value, final Provider provider) {
		final Nonce nonce = Nonce.builder()
			.value(value)
			.provider(provider)
			.build();

		nonceRepository.save(nonce);
	}

	@Transactional
	public void deleteNonce(final String value) {
		nonceRepository.deleteById(value);
	}

}
