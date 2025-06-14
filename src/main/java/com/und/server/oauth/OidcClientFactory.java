package com.und.server.oauth;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

@Component
public class OidcClientFactory {

	private final Map<Provider, OidcClient> oidcClients;

	public OidcClientFactory(KakaoClient kakaoClient) {
		oidcClients = new EnumMap<>(Provider.class);
		oidcClients.put(Provider.KAKAO, kakaoClient);
	}

	public OidcClient getOidcClient(Provider provider) {
		OidcClient oidcClient = oidcClients.get(provider);
		if (oidcClient == null) {
			throw new ServerException(ServerErrorResult.INVALID_PROVIDER);
		}

		return oidcClient;
	}

}
