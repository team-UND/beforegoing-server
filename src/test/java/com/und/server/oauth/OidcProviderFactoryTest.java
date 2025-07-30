package com.und.server.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
	@DisplayName("Throws an exception when the provider is null")
	void Given_NullProvider_When_GetIdTokenPayload_Then_ThrowsServerException() {
		// when & then
		assertThatThrownBy(() -> factory.getIdTokenPayload(null, token, oidcPublicKeys))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Retrieves ID token payload successfully for a given provider")
	void Given_ValidProvider_When_GetIdTokenPayload_Then_ReturnsCorrectPayload() {
		// given
		final IdTokenPayload expectedPayload = new IdTokenPayload(providerId, nickname);

		doReturn(expectedPayload).when(kakaoProvider).getIdTokenPayload(token, oidcPublicKeys);

		// when
		final IdTokenPayload actualPayload = factory.getIdTokenPayload(Provider.KAKAO, token, oidcPublicKeys);

		// then
		assertThat(actualPayload).isEqualTo(expectedPayload);
	}

}
