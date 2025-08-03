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

	private OidcClientFactory oidcClientFactory;

	@BeforeEach
	void init() {
		oidcClientFactory = new OidcClientFactory(kakaoClient);
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
	@DisplayName("Returns the correct OIDC client for a given provider")
	void Given_ValidProvider_When_GetOidcClient_Then_ReturnsCorrectClient() {
		// when
		final OidcClient client = oidcClientFactory.getOidcClient(Provider.KAKAO);

		// then
		assertThat(client).isEqualTo(kakaoClient);
	}

}
