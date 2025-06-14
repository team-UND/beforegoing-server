package com.und.server.oauth;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.dto.OidcPublicKey;
import com.und.server.dto.OidcPublicKeys;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

@Component
public class PublicKeyProvider {

	public PublicKey generatePublicKey(Map<String, String> decodedHeader, OidcPublicKeys oidcPublicKeys) {
		final OidcPublicKey matchingKey = oidcPublicKeys
			.matchingKey(decodedHeader.get("kid"), decodedHeader.get("alg"));

		return getPublicKey(matchingKey);
	}

	private PublicKey getPublicKey(final OidcPublicKey matchingKey) {
		try {
			byte[] modulusBytes = Base64.getUrlDecoder().decode(matchingKey.n());
			byte[] exponentBytes = Base64.getUrlDecoder().decode(matchingKey.e());

			final BigInteger modulus = new BigInteger(1, modulusBytes);
			final BigInteger exponent = new BigInteger(1, exponentBytes);
			final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);

			return KeyFactory.getInstance(matchingKey.kty()).generatePublic(publicKeySpec);
		} catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new ServerException(ServerErrorResult.INVALID_PUBLIC_KEY);
		}
	}

}
