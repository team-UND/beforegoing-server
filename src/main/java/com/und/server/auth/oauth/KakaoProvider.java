package com.und.server.auth.oauth;

import java.security.PublicKey;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.auth.jwt.JwtProvider;

@Component
public class KakaoProvider implements OidcProvider {

	private final JwtProvider jwtProvider;
	private final PublicKeyProvider publicKeyProvider;
	private final String kakaoBaseUrl;
	private final String kakaoAppKey;

	public KakaoProvider(
		final JwtProvider jwtProvider,
		final PublicKeyProvider publicKeyProvider,
		@Value("${oauth.kakao.base-url}") final String kakaoBaseUrl,
		@Value("${oauth.kakao.app-key}") final String kakaoAppKey
	) {
		this.jwtProvider = jwtProvider;
		this.publicKeyProvider = publicKeyProvider;
		this.kakaoBaseUrl = kakaoBaseUrl;
		this.kakaoAppKey = kakaoAppKey;
	}

	@Override
	public String getProviderId(final String token, final OidcPublicKeys oidcPublicKeys) {
		final Map<String, String> decodedHeader = jwtProvider.getDecodedHeader(token);
		final PublicKey publicKey = publicKeyProvider.generatePublicKey(decodedHeader, oidcPublicKeys);

		return jwtProvider.parseOidcIdToken(
			token,
			kakaoBaseUrl,
			kakaoAppKey,
			publicKey
		);
	}

}
