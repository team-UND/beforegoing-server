package com.und.server.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.dto.OidcPublicKeys;
import com.und.server.jwt.JwtProvider;

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

	@BeforeEach
	void init() {
		kakaoProvider = new KakaoProvider(jwtProvider, publicKeyProvider, kakaoBaseUrl, kakaoAppKey);
	}

	@Test
	void getOidcProviderIdSuccessfully() {
		final Map<String, String> decodedHeader = Map.of("alg", "RS256", "kid", "key1");
		final String subject = "166959";
		final String issuer = kakaoBaseUrl;
		final String audience = kakaoAppKey;

		// given
		when(jwtProvider.getDecodedHeader(token)).thenReturn(decodedHeader);
		when(publicKeyProvider.generatePublicKey(decodedHeader, oidcPublicKeys)).thenReturn(publicKey);
		when(jwtProvider.parseOidcSubjectFromIdToken(token, issuer, audience, publicKey))
			.thenReturn(subject);

		// when
		final String actualSubject = kakaoProvider.getOidcProviderId(token, oidcPublicKeys);

		// then
		assertThat(actualSubject).isEqualTo(subject);
	}
}
