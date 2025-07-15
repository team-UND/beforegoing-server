package com.und.server.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

class OidcPublicKeysTest {

	@Test
	@DisplayName("Return matching key when both kid and alg match")
	void returnMatchingKeyWhenKidAndAlgMatch() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "n1", "e1");
		final OidcPublicKey key2 = new OidcPublicKey("kid2", "RSA", "RS256", "sig", "n2", "e2");
		final OidcPublicKeys oidcPublicKeys = new OidcPublicKeys(List.of(key1, key2));

		// when
		final OidcPublicKey result = oidcPublicKeys.matchingKey("kid2", "RS256");

		// then
		assertThat(result).isEqualTo(key2);
	}

	@Test
	@DisplayName("Throw exception when kid matches but alg does not")
	void throwExceptionWhenKidMatchesButAlgDoesNot() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "n1", "e1");
		final OidcPublicKeys oidcPublicKeys = new OidcPublicKeys(List.of(key1));

		// when & then
		assertThatThrownBy(() -> oidcPublicKeys.matchingKey("kid1", "RS512"))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.PUBLIC_KEY_NOT_FOUND);
	}

	@Test
	@DisplayName("Throw exception when kid does not match")
	void throwExceptionWhenKidDoesNotMatch() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "n1", "e1");
		final OidcPublicKeys oidcPublicKeys = new OidcPublicKeys(List.of(key1));

		// when & then
		assertThatThrownBy(() -> oidcPublicKeys.matchingKey("kid3", "RS256"))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.PUBLIC_KEY_NOT_FOUND);
	}

}
