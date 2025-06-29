package com.und.server.oauth;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.dto.OidcPublicKeys;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

@Component
public class OidcProviderFactory {

	private final Map<Provider, OidcProvider> oidcProviders;

	public OidcProviderFactory(KakaoProvider kakaoProvider) {
		this.oidcProviders = new EnumMap<>(Provider.class);
		oidcProviders.put(Provider.KAKAO, kakaoProvider);
	}

	public IdTokenPayload getIdTokenPayload(
		Provider provider,
		String token,
		OidcPublicKeys oidcPublicKeys
	) {
		return getOidcProvider(provider).getIdTokenPayload(token, oidcPublicKeys);
	}

	private OidcProvider getOidcProvider(final Provider provider) {
		final OidcProvider oidcProvider = oidcProviders.get(provider);
		if (oidcProvider == null) {
			throw new ServerException(ServerErrorResult.INVALID_PROVIDER);
		}

		return oidcProvider;
	}

}
