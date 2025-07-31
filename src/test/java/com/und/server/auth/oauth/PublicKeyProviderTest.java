package com.und.server.auth.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import java.security.PublicKey;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.auth.dto.OidcPublicKey;
import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.common.exception.ServerErrorResult;
import com.und.server.common.exception.ServerException;

@ExtendWith(MockitoExtension.class)
class PublicKeyProviderTest {
	private final PublicKeyProvider publicKeyProvider = new PublicKeyProvider();

	@Mock
	private OidcPublicKeys oidcPublicKeys;

	@Mock
	private OidcPublicKey oidcPublicKey;

	private final Map<String, String> header = Map.of(
		"kid", "dummyKid", "alg", "RS256"
	);

	@Test
	@DisplayName("Throws an exception when OIDC key components are invalid")
	void Given_InvalidOidcKeyComponents_When_GeneratePublicKey_Then_ThrowsServerException() {
		// given
		doReturn(oidcPublicKey).when(oidcPublicKeys).matchingKey("dummyKid", "RS256");
		doReturn("!!!invalidbase64").when(oidcPublicKey).n();

		// then
		assertThatThrownBy(() -> publicKeyProvider.generatePublicKey(header, oidcPublicKeys))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_PUBLIC_KEY);
	}

	@Test
	@DisplayName("Generates a public key successfully from valid OIDC key components")
	void Given_ValidOidcKey_When_GeneratePublicKey_Then_ReturnsRsaPublicKey() {
		// given
		final String dummyN = "sXchJisJXZcWT7_GXjeYGv9dtGdAj4kmK6jRKgEZMpYl5izALeGaMWv6HvVb9s2AOjYX-5hykqCHrpb06XtQmQ";
		final String dummyE = "AQAB";

		doReturn(oidcPublicKey).when(oidcPublicKeys).matchingKey("dummyKid", "RS256");
		doReturn(dummyN).when(oidcPublicKey).n();
		doReturn(dummyE).when(oidcPublicKey).e();
		doReturn("RSA").when(oidcPublicKey).kty();

		// when
		final PublicKey publicKey = publicKeyProvider.generatePublicKey(header, oidcPublicKeys);

		// then
		assertThat(publicKey).isNotNull();
		assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
	}

}
