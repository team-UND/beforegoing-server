package com.und.server.auth.oauth;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.common.exception.ServerException;

@Component
public class OidcClientFactory {

	private final Map<Provider, OidcClient> oidcClients;

	public OidcClientFactory(final KakaoClient kakaoClient) {
		oidcClients = new EnumMap<>(Provider.class);
		oidcClients.put(Provider.KAKAO, kakaoClient);
	}

	public OidcClient getOidcClient(final Provider provider) {
		return Optional.ofNullable(oidcClients.get(provider))
			.orElseThrow(() -> new ServerException(AuthErrorResult.INVALID_PROVIDER));
	}

}
