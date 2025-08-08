package com.und.server.auth.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.common.exception.ServerException;

@ExtendWith(MockitoExtension.class)
class OidcClientFactoryTest {

	@Mock
	private KakaoClient kakaoClient;

	@Mock
	private AppleClient appleClient;

	private OidcClientFactory oidcClientFactory;

	@BeforeEach
	void init() {
		oidcClientFactory = new OidcClientFactory(kakaoClient, appleClient);
	}

	@Test
	@DisplayName("Throws an exception when the provider is null")
	void Given_NullProvider_When_GetOidcClient_Then_ThrowsServerException() {
		// when & then
		assertThatThrownBy(() -> oidcClientFactory.getOidcClient(null))
				.isInstanceOf(ServerException.class)
				.hasFieldOrPropertyWithValue("errorResult", AuthErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Returns the Kakao client for the KAKAO provider")
	void Given_KakaoProvider_When_GetOidcClient_Then_ReturnsKakaoClient() {
		// when
		final OidcClient client = oidcClientFactory.getOidcClient(Provider.KAKAO);

		// then
		assertThat(client).isEqualTo(kakaoClient);
	}

	@Test
	@DisplayName("Returns the Apple client for the APPLE provider")
	void Given_AppleProvider_When_GetOidcClient_Then_ReturnsAppleClient() {
		// when
		final OidcClient client = oidcClientFactory.getOidcClient(Provider.APPLE);

		// then
		assertThat(client).isEqualTo(appleClient);
	}

}
