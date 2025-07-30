package com.und.server.jwt;

public record ParsedTokenInfo(
	Long memberId,
	boolean isExpired
) { }
