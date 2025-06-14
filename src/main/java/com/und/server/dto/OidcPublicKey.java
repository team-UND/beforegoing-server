package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OidcPublicKey(
	@JsonProperty("kid") String kid,
	@JsonProperty("kty") String kty,
	@JsonProperty("alg") String alg,
	@JsonProperty("use") String use,
	@JsonProperty("n") String n,
	@JsonProperty("e") String e
) { }
