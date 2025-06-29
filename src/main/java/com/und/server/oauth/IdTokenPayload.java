package com.und.server.oauth;

public record IdTokenPayload(
	String providerId,
	String nickname
) { }
