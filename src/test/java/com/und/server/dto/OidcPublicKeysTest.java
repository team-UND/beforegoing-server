package com.und.server.dto;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

public class OidcPublicKeysTest {

	@Test
	void returnsMatchingKeySuccessfully() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "modulus1", "exponent1");
		final OidcPublicKey key2 = new OidcPublicKey("kid2", "RSA", "RS512", "sig", "modulus2", "exponent2");
		final OidcPublicKeys keys = new OidcPublicKeys(List.of(key1, key2));

		// when
		OidcPublicKey result = keys.matchingKey("kid2", "RS512");

		// then
		assertThat(result).isEqualTo(key2);
	}

	@Test
	void throwsExceptionWhenNoMatchingKeyFound() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "modulus1", "exponent1");
		final OidcPublicKeys keys = new OidcPublicKeys(List.of(key1));

		// when, then
		assertThatThrownBy(() -> keys.matchingKey("kidX", "RS256"))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.PUBLIC_KEY_NOT_FOUND);
	}
}
