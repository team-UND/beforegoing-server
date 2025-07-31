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
class KakaoProviderTest {

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private PublicKeyProvider publicKeyProvider;

	@Mock
	private OidcPublicKeys oidcPublicKeys;

	@Mock
	private PublicKey publicKey;

	private KakaoProvider kakaoProvider;

	private final String token = "dummyToken";
	private final String kakaoBaseUrl = "https://kauth.kakao.com";
	private final String kakaoAppKey = "dummyAppKey";
	private final String providerId = "dummyId";
	private final String nickname = "dummyNickname";

	@BeforeEach
	void init() {
		kakaoProvider = new KakaoProvider(jwtProvider, publicKeyProvider, kakaoBaseUrl, kakaoAppKey);
	}

	@Test
	@DisplayName("Successfully retrieves the ID token payload from a valid token")
	void Given_ValidToken_When_GetIdTokenPayload_Then_ReturnsCorrectPayload() {
		// given
		final Map<String, String> decodedHeader = Map.of("alg", "RS256", "kid", "key1");
		final IdTokenPayload expectedPayload = new IdTokenPayload(providerId, nickname);

		doReturn(decodedHeader).when(jwtProvider).getDecodedHeader(token);
		doReturn(publicKey).when(publicKeyProvider).generatePublicKey(decodedHeader, oidcPublicKeys);
		doReturn(expectedPayload).when(jwtProvider).parseOidcIdToken(token, kakaoBaseUrl, kakaoAppKey, publicKey);

		// when
		final IdTokenPayload actualPayload = kakaoProvider.getIdTokenPayload(token, oidcPublicKeys);

		// then
		assertThat(actualPayload).isEqualTo(expectedPayload);
	}

}
