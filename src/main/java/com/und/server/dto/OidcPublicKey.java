package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OIDC Public Key Details")
public record OidcPublicKey(
	@Schema(description = "Key ID", example = "a1b2c3d4e5")
	@JsonProperty("kid") String kid,

	@Schema(description = "Key Type", example = "RSA")
	@JsonProperty("kty") String kty,

	@Schema(description = "Algorithm", example = "RS256")
	@JsonProperty("alg") String alg,

	@Schema(description = "Usage", example = "sig")
	@JsonProperty("use") String use,

	@Schema(description = "Modulus", example = "u2a...")
	@JsonProperty("n") String n,

	@Schema(description = "Exponent", example = "AQAB")
	@JsonProperty("e") String e
) { }
