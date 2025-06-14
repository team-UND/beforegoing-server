package com.und.server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

public record OidcPublicKeys(@JsonProperty("keys") List<OidcPublicKey> keys) {

	public OidcPublicKey matchingKey(final String kid, final String alg) {
		return keys.stream()
			.filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
			.findAny()
			.orElseThrow(() -> new ServerException(ServerErrorResult.PUBLIC_KEY_NOT_FOUND));
	}

}
