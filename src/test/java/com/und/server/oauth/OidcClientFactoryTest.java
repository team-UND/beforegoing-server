package com.und.server.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

class OidcClientFactoryTest {

	@Mock
	private KakaoClient kakaoClient;

	private OidcClientFactory oidcClientFactory;

	@BeforeEach
	void init() {
		MockitoAnnotations.openMocks(this);
		oidcClientFactory = new OidcClientFactory(kakaoClient);
	}

	@Test
	void getOidcClientSuccessfully() {
		// when
		final OidcClient client = oidcClientFactory.getOidcClient(Provider.KAKAO);

		// then
		assertThat(client).isNotNull();
		assertThat(client).isEqualTo(kakaoClient);
	}

	@Test
	void throwExceptionWhenProviderIsNull() {
		// when & then
		assertThatThrownBy(() -> oidcClientFactory.getOidcClient(null))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.INVALID_PROVIDER);
	}

}
