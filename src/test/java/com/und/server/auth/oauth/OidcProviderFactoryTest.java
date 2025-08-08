package com.und.server.auth.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.common.exception.ServerException;

@ExtendWith(MockitoExtension.class)
class OidcProviderFactoryTest {

	@Mock
	private KakaoProvider kakaoProvider;

	@Mock
	private AppleProvider appleProvider;

	@Mock
	private OidcPublicKeys oidcPublicKeys;

	private OidcProviderFactory factory;

	private final String token = "dummyToken";
	private final String providerId = "dummyId";

	@BeforeEach
	void init() {
		factory = new OidcProviderFactory(kakaoProvider, appleProvider);
	}

	@Test
	@DisplayName("Throws an exception when the provider is null")
	void Given_NullProvider_When_GetProviderId_Then_ThrowsServerException() {
		// when & then
		assertThatThrownBy(() -> factory.getProviderId(null, token, oidcPublicKeys))
				.isInstanceOf(ServerException.class)
				.hasFieldOrPropertyWithValue("errorResult", AuthErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Retrieves Provider ID successfully for the Kakao provider")
	void Given_KakaoProvider_When_GetProviderId_Then_ReturnsCorrectProviderId() {
		// given
		doReturn(providerId).when(kakaoProvider).getProviderId(token, oidcPublicKeys);

		// when
		final String actualProviderId = factory.getProviderId(Provider.KAKAO, token, oidcPublicKeys);

		// then
		assertThat(actualProviderId).isEqualTo(providerId);
	}

	@Test
	@DisplayName("Retrieves Provider ID successfully for the Apple provider")
	void Given_AppleProvider_When_GetProviderId_Then_ReturnsCorrectProviderId() {
		// given
		doReturn(providerId).when(appleProvider).getProviderId(token, oidcPublicKeys);

		// when
		final String actualProviderId = factory.getProviderId(Provider.APPLE, token, oidcPublicKeys);

		// then
		assertThat(actualProviderId).isEqualTo(providerId);
	}

}
