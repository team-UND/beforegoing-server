package com.und.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OIDC Public Key Details")
public record OidcPublicKey(
	@Schema(description = "Key ID", example = "a1b2c3d4e5")
	String kid,

	@Schema(description = "Key Type", example = "RSA")
	String kty,

	@Schema(description = "Algorithm", example = "RS256")
	String alg,

	@Schema(description = "Usage", example = "sig")
	String use,

	@Schema(description = "Modulus", example = "q8zZ0b_MNaLd6Ny8wd4...")
	String n,

	@Schema(description = "Exponent", example = "AQAB")
	String e
) { }
