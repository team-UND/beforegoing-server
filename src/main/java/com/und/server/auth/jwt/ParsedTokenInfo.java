package com.und.server.auth.jwt;

public record ParsedTokenInfo(
	Long memberId,
	boolean isExpired
) { }
