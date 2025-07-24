package com.und.server.jwt;

import static org.assertj.core.api.Assertions.*;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.security.WeakKeyException;

class JwtPropertiesTest {

	@Test
	@DisplayName("Throws WeakKeyException when the secret string is too short")
	void Given_ShortSecretString_When_GetSecretKey_Then_ThrowsWeakKeyException() {
		// given
		final String shortSecret = "short";
		final JwtProperties jwtProperties = new JwtProperties(
			"Authorization", "Bearer", "issuer",
			shortSecret, 3600, 7200
		);

		// when, then
		assertThatThrownBy(jwtProperties::secretKey)
			.isInstanceOf(WeakKeyException.class)
			.hasMessageContaining("The specified key byte array is");
	}

	@Test
	@DisplayName("Returns SecretKey from a valid secret string")
	void Given_ValidSecretString_When_GetSecretKey_Then_ReturnsHmacSha256Key() {
		// given
		final String secret = "12345678901234567890123456789012";
		final JwtProperties jwtProperties = new JwtProperties(
			"Authorization", "Bearer", "issuer",
			secret, 3600, 7200
		);

		// when
		final SecretKey secretKey = jwtProperties.secretKey();

		// then
		assertThat(secretKey).isNotNull();
		assertThat(secretKey.getAlgorithm()).isEqualTo("HmacSHA256");
	}

}
