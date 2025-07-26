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
import com.und.server.oauth.IdTokenPayload;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private final JwtProperties jwtProperties;

	public Map<String, String> getDecodedHeader(final String token) {
		try {
			String decodedHeader = decodeBase64UrlPart(token.split("\\.")[0]);
			return new ObjectMapper().readValue(decodedHeader, new TypeReference<>() { });
		} catch (Exception e) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}
	}

	public String extractNonce(String idToken) {
		try {
			final String payloadJson = decodeBase64UrlPart(idToken.split("\\.")[1]);
			final Map<String, Object> claims = new ObjectMapper().readValue(payloadJson, new TypeReference<>() { });
			return (String) claims.get("nonce");
		} catch (Exception e) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}
	}

	public IdTokenPayload parseOidcIdToken(
			final String token,
			final String iss,
			final String aud,
			final PublicKey publicKey
	) {
		JwtParserBuilder builder = Jwts.parser()
				.verifyWith(publicKey)
				.requireIssuer(iss)
				.requireAudience(aud);

		final Claims claims = parseClaims(token, builder);

		return new IdTokenPayload(claims.getSubject(), claims.get("nickname", String.class));
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
			return parseToken(token, builder);
		} catch (ExpiredJwtException e) {
			throw new ServerException(ServerErrorResult.EXPIRED_TOKEN, e);
		}
	}

	public Long getMemberIdFromExpiredAccessToken(final String token) {
		final JwtParserBuilder builder = Jwts.parser().verifyWith(jwtProperties.secretKey());
		try {
			parseToken(token, builder);
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		} catch (ExpiredJwtException e) {
			return Long.valueOf(e.getClaims().getSubject());
		}
	}

	private Claims parseToken(final String token, final JwtParserBuilder builder) {
		try {
			return builder.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (MalformedJwtException e) {
			throw new ServerException(ServerErrorResult.MALFORMED_TOKEN, e);
		} catch (SecurityException e) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN_SIGNATURE, e);
		} catch (ExpiredJwtException e) {
			throw e;
		} catch (JwtException e) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}
	}

	private String decodeBase64UrlPart(String encodedPart) {
		return new String(Decoders.BASE64URL.decode(encodedPart), StandardCharsets.UTF_8);
	}

	private Date toDate(final LocalDateTime dateTime) {
		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

}
