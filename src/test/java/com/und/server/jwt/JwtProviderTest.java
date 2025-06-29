package com.und.server.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.oauth.IdTokenPayload;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

	@Mock
	private JwtProperties jwtProperties;

	private JwtProvider jwtProvider;

	private SecretKey secretKey;
	private final String nonce = "dummyNonce";
	private final String issuer = "dummyIssuer";
	private final String subject = "Chori";
	private final String audience = "client-id";

	@BeforeEach
	void init() {
		secretKey = Jwts.SIG.HS256.key().build();
		jwtProvider = new JwtProvider(jwtProperties);
	}

	@Test
	void throwExceptionWhenTokenFormatIsInvalid() {
		// given
		final String invalidToken = "invalidHeaderOnly";

		// when & then
		assertThatThrownBy(() -> jwtProvider.getDecodedHeader(invalidToken))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	void throwExceptionWhenBase64HeaderIsInvalidInvalid() {
		// given
		final String malformedBase64Header = "!!!";
		final String token = malformedBase64Header + ".payload.signature";

		// when & then
		assertThatThrownBy(() -> jwtProvider.getDecodedHeader(token))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	void extractNonceSuccessfully() throws Exception {
		// given
		final String token = Jwts.builder()
				.subject("1")
				.claim("nonce", nonce)
				.signWith(secretKey)
				.compact();

		// when
		final String extractedNonce = jwtProvider.extractNonce(token);

		// then
		assertThat(extractedNonce).isEqualTo(nonce);
	}

	@Test
	void throwExceptionWhenTokenIsInvalid() {
		// given
		final String invalidToken = "invalid.token.parts";

		// when & then
		assertThatThrownBy(() -> jwtProvider.extractNonce(invalidToken))
				.isInstanceOf(ServerException.class)
				.extracting("errorResult")
				.isEqualTo(ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	void parseOidcIdTokenSuccessfully() throws Exception {
		// given
		final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		final String token = Jwts.builder()
				.subject(subject)
				.issuer(issuer)
				.audience()
					.add(audience)
					.and()
				.claim("nickname", "Chori")
				.issuedAt(new Date())
				.signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
				.compact();

		// when
		final IdTokenPayload payload = jwtProvider.parseOidcIdToken(
			token, issuer, audience, keyPair.getPublic()
		);

		// then
		assertThat(payload.providerId()).isEqualTo(subject);
		assertThat(payload.nickname()).isEqualTo("Chori");
	}

	@Test
	void throwExceptionWhenAudienceDoesNotMatch() throws Exception {
		// given
		final String wrongAudience = "wrong-client";
		final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		final PublicKey publicKey = keyPair.getPublic();
		final String token = Jwts.builder()
			.subject(subject)
			.issuer(issuer)
			.audience()
				.add(audience)
				.and()
			.issuedAt(new Date())
			.signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
			.compact();

		// when & then
		assertThatThrownBy(() -> {
			jwtProvider.parseOidcIdToken(token, issuer, wrongAudience, publicKey);
		}).isInstanceOf(ServerException.class);
	}

	@Test
	void generateAccessTokenWithValidClaims() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final Long memberId = 1L;

		// when
		final String token = jwtProvider.generateAccessToken(memberId);
		final Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

		// then
		assertThat(claims.getSubject()).isEqualTo(memberId.toString());
		assertThat(claims.getIssuer()).isEqualTo(issuer);
	}

	@Test
	void verifyTokenIssuedAtTimeConversion() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final Long memberId = 1L;
		final LocalDateTime beforeGeneration = LocalDateTime.now();

		// when
		final String token = jwtProvider.generateAccessToken(memberId);
		final Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
		final LocalDateTime afterGeneration = LocalDateTime.now();

		// then
		final Date issuedAt = claims.getIssuedAt();
		final LocalDateTime issuedAtDateTime = issuedAt.toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDateTime();

		assertThat(issuedAtDateTime)
			.isAfterOrEqualTo(beforeGeneration.minusSeconds(1))
			.isBeforeOrEqualTo(afterGeneration.plusSeconds(1));
	}

	@Test
	void verifyTimeZoneConsistencyInTokenGeneration() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final Long memberId = 1L;

		// when
		final String token = jwtProvider.generateAccessToken(memberId);
		final Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

		// then
		final Date issuedAt = claims.getIssuedAt();
		final Date expiration = claims.getExpiration();
		final long timeDifferenceInSeconds = (expiration.getTime() - issuedAt.getTime()) / 1000;
		assertThat(timeDifferenceInSeconds).isEqualTo(3600);
	}

	@Test
	void getDecodedHeaderSuccessfully() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final String token = jwtProvider.generateAccessToken(1L);

		// when
		final Map<String, String> header = jwtProvider.getDecodedHeader(token);

		// then
		assertThat(header.get("alg")).isNotBlank();
	}

	@Test
	void getAuthentication() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final Long memberId = 1L;
		final String token = jwtProvider.generateAccessToken(memberId);

		// when
		final Authentication authentication = jwtProvider.getAuthentication(token);

		// then
		assertThat(authentication.getPrincipal()).isEqualTo(memberId);
		assertThat(authentication.getCredentials()).isEqualTo(token);
	}

	@Test
	void getMemberIdFromToken() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final Long memberId = 1L;
		final String token = jwtProvider.generateAccessToken(memberId);

		// when
		final Long extractedId = jwtProvider.getMemberIdFromToken(token);

		// then
		assertThat(extractedId).isEqualTo(memberId);
	}

	@Test
	void throwsTokenNotExpiredException() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final String token = jwtProvider.generateAccessToken(1L);

		// when & then
		assertThatThrownBy(() -> jwtProvider.getMemberIdFromExpiredAccessToken(token))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.TOKEN_NOT_EXPIRED);
	}

	@Test
	void throwExpiredTokenException() throws Exception {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();

		final Long memberId = 1L;
		final Date now = new Date();
		final Date issuedAt = new Date(now.getTime() - 10000);
		final Date expiredAt = new Date(now.getTime() - 5000);
		final String token = Jwts.builder()
			.subject(memberId.toString())
			.issuer(issuer)
			.issuedAt(issuedAt)
			.expiration(expiredAt)
			.signWith(secretKey)
			.compact();

		// when & then
		assertThatThrownBy(() -> jwtProvider.getMemberIdFromToken(token))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(ServerErrorResult.EXPIRED_TOKEN);
	}

	@Test
	void getMemberIdFromExpiredAccessTokenSuccessfully() throws Exception {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();

		final Long memberId = 42L;
		final Date now = new Date();
		final Date issuedAt = new Date(now.getTime() - 10000);
		final Date expiredAt = new Date(now.getTime() - 5000);
		final String token = Jwts.builder()
			.subject(memberId.toString())
			.issuer(issuer)
			.issuedAt(issuedAt)
			.expiration(expiredAt)
			.signWith(secretKey)
			.compact();

		// when
		final Long extractedId = jwtProvider.getMemberIdFromExpiredAccessToken(token);

		// then
		assertThat(extractedId).isEqualTo(memberId);
	}

}
