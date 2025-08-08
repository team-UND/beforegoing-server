package com.und.server.auth.dto;

import java.util.List;

import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.common.exception.ServerException;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A list of OIDC Public Keys")
public record OidcPublicKeys(
	@Schema(description = "List of public keys", example = "[...]")
	List<OidcPublicKey> keys
) {
	public OidcPublicKey matchingKey(final String kid, final String alg) {
		return keys.stream()
			.filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
			.findAny()
			.orElseThrow(() -> new ServerException(AuthErrorResult.PUBLIC_KEY_NOT_FOUND));
	}
}
