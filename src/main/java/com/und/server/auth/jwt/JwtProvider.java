package com.und.server.auth.jwt;

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
import com.und.server.auth.oauth.IdTokenPayload;
import com.und.server.common.exception.ServerErrorResult;
import com.und.server.common.exception.ServerException;
import com.und.server.common.util.ProfileManager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private final JwtProperties jwtProperties;
	private final ProfileManager profileManager;

	public Map<String, String> getDecodedHeader(final String token) {
		try {
			final String decodedHeader = decodeBase64UrlPart(token.split("\\.")[0]);
			return new ObjectMapper().readValue(decodedHeader, new TypeReference<>() { });
		} catch (final Exception e) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}
	}

	public String extractNonce(final String idToken) {
		try {
			final String payloadJson = decodeBase64UrlPart(idToken.split("\\.")[1]);
			final Map<String, Object> claims = new ObjectMapper().readValue(payloadJson, new TypeReference<>() { });
			return (String) claims.get("nonce");
		} catch (final Exception e) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}
	}

	public IdTokenPayload parseOidcIdToken(
			final String token,
			final String iss,
			final String aud,
			final PublicKey publicKey
	) {
		final JwtParserBuilder builder = Jwts.parser()
				.verifyWith(publicKey)
				.requireIssuer(iss)
				.requireAudience(aud);
		final Claims claims = parseClaims(token, builder);

		return new IdTokenPayload(
			getValidSubject(claims),
			claims.get("nickname", String.class)
		);
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
		final Claims claims = parseClaims(token, getAccessTokenParserBuilder());
		return getMemberIdFromClaims(claims);
	}

	private Claims parseClaims(final String token, final JwtParserBuilder builder) {
		try {
			return parseToken(token, builder);
		} catch (final ExpiredJwtException e) {
			throw new ServerException(ServerErrorResult.EXPIRED_TOKEN, e);
		}
	}

	public ParsedTokenInfo parseTokenForReissue(final String token) {
		try {
			final Claims claims = parseToken(token, getAccessTokenParserBuilder());
			return new ParsedTokenInfo(getMemberIdFromClaims(claims), false);
		} catch (final ExpiredJwtException e) {
			// If the token is expired, we can still extract the member ID.
			return new ParsedTokenInfo(getMemberIdFromClaims(e.getClaims()), true);
		}
	}

	private Claims parseToken(final String token, final JwtParserBuilder builder) {
		try {
			return builder.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (final ExpiredJwtException e) {
			// This must be re-thrown for parseTokenForReissue to work correctly.
			throw e;
		} catch (final JwtException e) {
			// For prod or stg environments, return a generic error to avoid leaking details.
			if (profileManager.isProdOrStgProfile()) {
				throw new ServerException(ServerErrorResult.UNAUTHORIZED_ACCESS, e);
			}

			// For non-production environments, provide detailed error messages.
			if (e instanceof MalformedJwtException) {
				throw new ServerException(ServerErrorResult.MALFORMED_TOKEN, e);
			}
			if (e instanceof UnsupportedJwtException) {
				throw new ServerException(ServerErrorResult.UNSUPPORTED_TOKEN, e);
			}
			if (e instanceof WeakKeyException) {
				throw new ServerException(ServerErrorResult.WEAK_TOKEN_KEY, e);
			}
			if (e instanceof SignatureException) {
				throw new ServerException(ServerErrorResult.INVALID_TOKEN_SIGNATURE, e);
			}
			// Fallback for any other JWT-related exceptions.
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}
	}

	private JwtParserBuilder getAccessTokenParserBuilder() {
		return Jwts.parser()
			.verifyWith(jwtProperties.secretKey());
	}

	private String decodeBase64UrlPart(final String encodedPart) {
		return new String(Decoders.BASE64URL.decode(encodedPart), StandardCharsets.UTF_8);
	}

	private Date toDate(final LocalDateTime dateTime) {
		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private Long getMemberIdFromClaims(final Claims claims) {
		final String subject = getValidSubject(claims);
		try {
			return Long.valueOf(subject);
		} catch (final NumberFormatException e) {
			// The subject was not a valid Long, which is unexpected for our tokens.
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}
	}

	private String getValidSubject(final Claims claims) {
		final String subject = claims.getSubject();
		if (subject == null) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}
		return subject;
	}

}
