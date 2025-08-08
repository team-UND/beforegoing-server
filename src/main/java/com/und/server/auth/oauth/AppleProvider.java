package com.und.server.auth.oauth;

import java.security.PublicKey;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.auth.jwt.JwtProvider;

@Component
public class AppleProvider implements OidcProvider {

	private final JwtProvider jwtProvider;
	private final PublicKeyProvider publicKeyProvider;
	private final String appleBaseUrl;
	private final String appleAppId;

	public AppleProvider(
			final JwtProvider jwtProvider,
			final PublicKeyProvider publicKeyProvider,
			@Value("${oauth.apple.base-url}") final String appleBaseUrl,
			@Value("${oauth.apple.app-id}") final String appleAppId
	) {
		this.jwtProvider = jwtProvider;
		this.publicKeyProvider = publicKeyProvider;
		this.appleBaseUrl = appleBaseUrl;
		this.appleAppId = appleAppId;
	}

	@Override
	public String getProviderId(final String token, final OidcPublicKeys oidcPublicKeys) {
		final Map<String, String> decodedHeader = jwtProvider.getDecodedHeader(token);
		final PublicKey publicKey = publicKeyProvider.generatePublicKey(decodedHeader, oidcPublicKeys);

		return jwtProvider.parseOidcIdToken(
				token,
				appleBaseUrl,
				appleAppId,
				publicKey
		);
	}

}
