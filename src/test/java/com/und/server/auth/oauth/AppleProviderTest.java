package com.und.server.auth.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.security.PublicKey;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.auth.jwt.JwtProvider;

@ExtendWith(MockitoExtension.class)
class AppleProviderTest {

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private PublicKeyProvider publicKeyProvider;

	@Mock
	private OidcPublicKeys oidcPublicKeys;

	@Mock
	private PublicKey publicKey;

	private AppleProvider appleProvider;

	private final String token = "dummyToken";
	private final String appleBaseUrl = "https://appleid.apple.com";
	private final String appleAppId = "dummyAppId";
	private final String providerId = "dummyId";

	@BeforeEach
	void init() {
		appleProvider = new AppleProvider(jwtProvider, publicKeyProvider, appleBaseUrl, appleAppId);
	}

	@Test
	@DisplayName("Successfully retrieves the Provider ID from a valid token")
	void Given_ValidToken_When_GetProviderId_Then_ReturnsCorrectProviderId() {
		// given
		final Map<String, String> decodedHeader = Map.of("alg", "RS256", "kid", "key1");

		doReturn(decodedHeader).when(jwtProvider).getDecodedHeader(token);
		doReturn(publicKey).when(publicKeyProvider).generatePublicKey(decodedHeader, oidcPublicKeys);
		doReturn(providerId).when(jwtProvider).parseOidcIdToken(token, appleBaseUrl, appleAppId, publicKey);

		// when
		final String actualProviderId = appleProvider.getProviderId(token, oidcPublicKeys);

		// then
		assertThat(actualProviderId).isEqualTo(providerId);
	}

}
