package com.und.server.jwt;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

	private final JwtProperties jwtProperties;

	public Map<String, String> getDecodedHeader(final String token) {
		try {
			String decodedHeader = new String(
				Decoders.BASE64URL.decode(token.split("\\.")[0]), StandardCharsets.UTF_8
			);
			return new ObjectMapper().readValue(decodedHeader, new TypeReference<>() { });
		} catch (Exception e) {
			log.error("Failed to decode JWT header: {}", e.getMessage(), e);
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}
	}

	public String extractNonce(String idToken) {
		try {
			final String[] parts = idToken.split("\\.");
			final String payloadJson = new String(Decoders.BASE64URL.decode(parts[1]), StandardCharsets.UTF_8);
			final Map<String, Object> claims = new ObjectMapper().readValue(payloadJson, new TypeReference<>() { });
			return (String) claims.get("nonce");
		} catch (Exception e) {
			log.error("Failed to extract nonce: {}", e.getMessage(), e);
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}
	}

	public String parseOidcSubjectFromIdToken(
			final String token,
			final String iss,
			final String aud,
			final PublicKey publicKey
	) {
		JwtParserBuilder builder = Jwts.parser()
				.verifyWith(publicKey)
				.requireIssuer(iss)
				.requireAudience(aud);

		return parseClaims(token, builder).getSubject();
	}

	public String generateAccessToken(final Long memberId) {
		final LocalDateTime now = LocalDateTime.now();
		final Date issuedAt = toDate(now);
		final Date expiration = toDate(now.plusSeconds(jwtProperties.accessTokenExpireTime()));

		return Jwts.builder()
				.subject(memberId.toString())
				.issuer(jwtProperties.issuer())
				.issuedAt(issuedAt)
				.expiration(expiration)
				.signWith(jwtProperties.secretKey())
				.compact();
	}

	public Authentication getAuthentication(final String token) {
		final Long memberId = getMemberIdFromToken(token);
		final List<SimpleGrantedAuthority> authorities = Collections.emptyList();

		return new UsernamePasswordAuthenticationToken(memberId, token, authorities);
	}

	public Long getMemberIdFromToken(final String token) {
		return Long.valueOf(parseAccessTokenClaims(token).getSubject());
	}

	private Claims parseAccessTokenClaims(final String token) {
		JwtParserBuilder builder = Jwts.parser()
				.verifyWith(jwtProperties.secretKey());
		return parseClaims(token, builder);
	}

	private Claims parseClaims(final String token, final JwtParserBuilder builder) {
		try {
			return builder.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (ExpiredJwtException e) {
			log.warn("Expired JWT token - expiration time: {}, current time: {}",
				e.getClaims().getExpiration(), new Date());
			throw new ServerException(ServerErrorResult.EXPIRED_TOKEN);
		} catch (MalformedJwtException e) {
			log.error("Malformed JWT token: {}", e.getMessage());
			throw new ServerException(ServerErrorResult.MALFORMED_TOKEN);
		} catch (SecurityException e) {
			log.error("JWT signature verification failed: {}", e.getMessage());
			throw new ServerException(ServerErrorResult.INVALID_SIGNATURE);
		} catch (Exception e) {
			log.error("Unexpected error occurred while parsing JWT", e);
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}
	}

	public Long getMemberIdFromExpiredAccessToken(final String token) {
		try {
			Jwts.parser()
				.verifyWith(jwtProperties.secretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();

			throw new ServerException(ServerErrorResult.TOKEN_NOT_EXPIRED);
		} catch (ExpiredJwtException e) {
			return Long.valueOf(e.getClaims().getSubject());
		} catch (JwtException e) {
			log.error("Unexpected error occurred while parsing JWT", e);
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}
	}

	private Date toDate(final LocalDateTime dateTime) {
		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

}
