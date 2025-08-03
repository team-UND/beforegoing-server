package com.und.server.auth.oauth;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.auth.dto.OidcPublicKey;
import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.common.exception.ServerException;

@Component
public class PublicKeyProvider {

	public PublicKey generatePublicKey(final Map<String, String> decodedHeader, final OidcPublicKeys oidcPublicKeys) {
		final OidcPublicKey matchingKey = oidcPublicKeys
			.matchingKey(decodedHeader.get("kid"), decodedHeader.get("alg"));

		return getPublicKey(matchingKey);
	}

	private PublicKey getPublicKey(final OidcPublicKey matchingKey) {
		try {
			final byte[] modulusBytes = Base64.getUrlDecoder().decode(matchingKey.n());
			final byte[] exponentBytes = Base64.getUrlDecoder().decode(matchingKey.e());

			final BigInteger modulus = new BigInteger(1, modulusBytes);
			final BigInteger exponent = new BigInteger(1, exponentBytes);
			final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);

			return KeyFactory.getInstance(matchingKey.kty()).generatePublic(publicKeySpec);
		} catch (final IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new ServerException(AuthErrorResult.INVALID_PUBLIC_KEY, e);
		}
	}

}
