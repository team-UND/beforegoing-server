package com.und.server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A list of OIDC Public Keys")
public record OidcPublicKeys(
	@Schema(description = "List of public keys", example = "[...]")
	@JsonProperty("keys") List<OidcPublicKey> keys
) {
	public OidcPublicKey matchingKey(final String kid, final String alg) {
		return keys.stream()
			.filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
			.findAny()
			.orElseThrow(() -> new ServerException(ServerErrorResult.PUBLIC_KEY_NOT_FOUND));
	}
}
