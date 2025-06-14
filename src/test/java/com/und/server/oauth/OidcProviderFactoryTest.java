package com.und.server.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.dto.OidcPublicKeys;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

@ExtendWith(MockitoExtension.class)
class OidcProviderFactoryTest {

	@Mock
	private KakaoProvider kakaoProvider;

	@Mock
	private OidcPublicKeys oidcPublicKeys;

	private OidcProviderFactory factory;

	@BeforeEach
	void init() {
		factory = new OidcProviderFactory(kakaoProvider);
	}

	@Test
	void getOidcProviderIdSuccessfully() {
		final String token = "dummyToken";
		final String expectedId = "providerId123";

		when(kakaoProvider.getOidcProviderId(token, oidcPublicKeys)).thenReturn(expectedId);

		final String actualId = factory.getOidcProviderId(Provider.KAKAO, token, oidcPublicKeys);

		assertThat(actualId).isEqualTo(expectedId);
	}

	@Test
	void throwExceptionWhenProviderNotRegistered() {
		final String token = "dummyToken";

		assertThatThrownBy(() -> factory.getOidcProviderId(Provider.APPLE, token, oidcPublicKeys))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.INVALID_PROVIDER);
	}
}
