package com.und.server.oauth;

import java.security.PublicKey;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.und.server.dto.OidcPublicKeys;
import com.und.server.jwt.JwtProvider;

@Component
public class KakaoProvider implements OidcProvider {

	private final JwtProvider jwtProvider;
	private final PublicKeyProvider publicKeyProvider;
	private final String kakaoBaseUrl;
	private final String kakaoAppKey;

	public KakaoProvider(
		JwtProvider jwtProvider,
		PublicKeyProvider publicKeyProvider,
		@Value("${oauth.kakao.base-url}") String kakaoBaseUrl,
		@Value("${oauth.kakao.app-key}") String kakaoAppKey
	) {
		this.jwtProvider = jwtProvider;
		this.publicKeyProvider = publicKeyProvider;
		this.kakaoBaseUrl = kakaoBaseUrl;
		this.kakaoAppKey = kakaoAppKey;
	}

	@Override
	public String getOidcProviderId(String token, OidcPublicKeys oidcPublicKeys) {
		final Map<String, String> decodedHeader = jwtProvider.getDecodedHeader(token);
		final PublicKey publicKey = publicKeyProvider.generatePublicKey(decodedHeader, oidcPublicKeys);

		return jwtProvider.parseOidcSubjectFromIdToken(
			token,
			kakaoBaseUrl,
			kakaoAppKey,
			publicKey
		);
	}

}
