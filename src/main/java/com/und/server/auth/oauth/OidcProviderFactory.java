package com.und.server.auth.oauth;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.common.exception.ServerException;

@Component
public class OidcProviderFactory {

	private final Map<Provider, OidcProvider> oidcProviders;

	public OidcProviderFactory(
		final KakaoProvider kakaoProvider,
		final AppleProvider appleProvider
	) {
		this.oidcProviders = new EnumMap<>(Provider.class);
		oidcProviders.put(Provider.KAKAO, kakaoProvider);
		oidcProviders.put(Provider.APPLE, appleProvider);
	}

	public IdTokenPayload getIdTokenPayload(
		final Provider provider,
		final String token,
		final OidcPublicKeys oidcPublicKeys
	) {
		return getOidcProvider(provider).getIdTokenPayload(token, oidcPublicKeys);
	}

	private OidcProvider getOidcProvider(final Provider provider) {
		return Optional.ofNullable(oidcProviders.get(provider))
			.orElseThrow(() -> new ServerException(AuthErrorResult.INVALID_PROVIDER));
	}

}
