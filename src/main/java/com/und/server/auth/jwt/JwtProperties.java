package com.und.server.auth.jwt;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.jsonwebtoken.security.Keys;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	String header,
	String type,
	String issuer,
	String secret,
	Integer accessTokenExpireTime,
	Integer refreshTokenExpireTime
) {

	public SecretKey secretKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

}
