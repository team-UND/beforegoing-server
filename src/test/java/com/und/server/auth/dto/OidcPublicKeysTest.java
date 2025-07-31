package com.und.server.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.common.exception.ServerErrorResult;
import com.und.server.common.exception.ServerException;

class OidcPublicKeysTest {

	@Test
	@DisplayName("Throws an exception when kid does not match")
	void Given_MismatchedKid_When_MatchingKey_Then_ThrowsServerException() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "n1", "e1");
		final OidcPublicKeys oidcPublicKeys = new OidcPublicKeys(List.of(key1));

		// when & then
		assertThatThrownBy(() -> oidcPublicKeys.matchingKey("kid3", "RS256"))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.PUBLIC_KEY_NOT_FOUND);
	}

	@Test
	@DisplayName("Throws an exception when kid matches but alg does not")
	void Given_MismatchedAlg_When_MatchingKey_Then_ThrowsServerException() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "n1", "e1");
		final OidcPublicKeys oidcPublicKeys = new OidcPublicKeys(List.of(key1));

		// when & then
		assertThatThrownBy(() -> oidcPublicKeys.matchingKey("kid1", "RS512"))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.PUBLIC_KEY_NOT_FOUND);
	}

	@Test
	@DisplayName("Returns a matching key when both kid and alg match")
	void Given_MatchingKidAndAlg_When_MatchingKey_Then_ReturnsCorrectKey() {
		// given
		final OidcPublicKey key1 = new OidcPublicKey("kid1", "RSA", "RS256", "sig", "n1", "e1");
		final OidcPublicKey key2 = new OidcPublicKey("kid2", "RSA", "RS256", "sig", "n2", "e2");
		final OidcPublicKeys oidcPublicKeys = new OidcPublicKeys(List.of(key1, key2));

		// when
		final OidcPublicKey result = oidcPublicKeys.matchingKey("kid2", "RS256");

		// then
		assertThat(result).isEqualTo(key2);
	}

}
