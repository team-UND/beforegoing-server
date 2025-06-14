package com.und.server.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.dto.OidcPublicKey;
import com.und.server.dto.OidcPublicKeys;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

@ExtendWith(MockitoExtension.class)
public class PublicKeyProviderTest {
	private PublicKeyProvider publicKeyProvider;

	@Mock
	private OidcPublicKeys oidcPublicKeys;

	@Mock
	private OidcPublicKey oidcPublicKey;

	private Map<String, String> header;

	@BeforeEach
	void init() {
		publicKeyProvider = new PublicKeyProvider();

		header = new HashMap<>();
		header.put("kid", "dummyKid");
		header.put("alg", "RS256");
	}

	@Test
	void generatePublicKeySuccessfully() {
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

	@Test
	void throwExceptionWhenKeyInvalid() {
		// given
		doReturn(oidcPublicKey).when(oidcPublicKeys).matchingKey("dummyKid", "RS256");
		doReturn("!!!invalidbase64").when(oidcPublicKey).n();

		// then
		assertThatThrownBy(() -> publicKeyProvider.generatePublicKey(header, oidcPublicKeys))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.INVALID_PUBLIC_KEY);
	}
}
