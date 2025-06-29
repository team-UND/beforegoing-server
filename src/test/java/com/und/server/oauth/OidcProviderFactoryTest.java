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

	private final String token = "dummyToken";
	private final String providerId = "dummyId";
	private final String nickname = "dummyNickname";

	@BeforeEach
	void init() {
		factory = new OidcProviderFactory(kakaoProvider);
	}

	@Test
	void getIdTokenPayloadSuccessfully() {
		// given
		final IdTokenPayload expectedPayload = new IdTokenPayload(providerId, nickname);

		when(kakaoProvider.getIdTokenPayload(token, oidcPublicKeys)).thenReturn(expectedPayload);

		// when
		final IdTokenPayload actualPayload = factory.getIdTokenPayload(Provider.KAKAO, token, oidcPublicKeys);

		// then
		assertThat(actualPayload).isEqualTo(expectedPayload);
	}

	@Test
	void throwExceptionWhenProviderNotRegistered() {
		// when & then
		assertThatThrownBy(() -> factory.getIdTokenPayload(Provider.APPLE, token, oidcPublicKeys))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.INVALID_PROVIDER);
	}
}
