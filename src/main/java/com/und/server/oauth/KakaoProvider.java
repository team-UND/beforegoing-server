package com.und.server.oauth;

import java.security.PublicKey;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.dto.OidcPublicKeys;
import com.und.server.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoProvider implements OidcProvider {

	private final JwtProvider jwtProvider;
	private final PublicKeyProvider publicKeyProvider;

	@Override
	public String getOidcProviderId(String token, OidcPublicKeys oidcPublicKeys) {
		final Map<String, String> decodedHeader = jwtProvider.getDecodedHeader(token);
		final PublicKey publicKey = publicKeyProvider.generatePublicKey(decodedHeader, oidcPublicKeys);

		return jwtProvider.parseOidcSubjectFromIdToken(
			token,
			"${oauth.kakao.base-url}",
			"${oauth.kakao.app-key}",
			publicKey
		);
	}

}
