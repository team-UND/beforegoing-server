package com.und.server.auth.oauth;

public record IdTokenPayload(
	String providerId,
	String nickname
) { }
