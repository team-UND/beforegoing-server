package com.und.server.jwt;

import static org.assertj.core.api.Assertions.*;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.security.WeakKeyException;

public class JwtPropertiesTest {

	@Test
	void returnsSecretKeyFromSecretString() {
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

	@Test
	void throwsExceptionWhenSecretTooShort() {
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

}
