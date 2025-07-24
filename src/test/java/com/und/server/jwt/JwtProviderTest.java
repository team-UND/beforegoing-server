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
import org.junit.jupiter.api.DisplayName;
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
	@DisplayName("Throws ServerException when token format is invalid")
	void Given_InvalidFormatToken_When_GetDecodedHeader_Then_ThrowsServerException() {
		// given
		final String invalidToken = "invalidHeaderOnly";

		// when & then
		assertThatThrownBy(() -> jwtProvider.getDecodedHeader(invalidToken))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Throws ServerException when token header is not a valid Base64 format")
	void Given_MalformedBase64HeaderToken_When_GetDecodedHeader_Then_ThrowsServerException() {
		// given
		final String malformedBase64Header = "!!!";
		final String token = malformedBase64Header + ".payload.signature";

		// when & then
		assertThatThrownBy(() -> jwtProvider.getDecodedHeader(token))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Throws ServerException when nonce is not present in token")
	void Given_TokenWithoutNonce_When_ExtractNonce_Then_ThrowsServerException() {
		// given
		final String invalidToken = "invalid.token.parts";

		// when & then
		assertThatThrownBy(() -> jwtProvider.extractNonce(invalidToken))
				.isInstanceOf(ServerException.class)
				.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Extracts nonce successfully from a valid token")
	void Given_ValidTokenWithNonce_When_ExtractNonce_Then_ReturnsCorrectNonce() {
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
	@DisplayName("Throws ServerException when audience does not match")
	void Given_OidcToken_When_ParseWithMismatchedAudience_Then_ThrowsServerException() throws Exception {
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
		}).isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Parses a valid OIDC ID token successfully")
	void Given_ValidOidcIdToken_When_ParseOidcIdToken_Then_ReturnsCorrectPayload() throws Exception {
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
	@DisplayName("Generates access token with valid claims")
	void Given_MemberId_When_GenerateAccessToken_Then_TokenContainsValidClaims() {
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
	@DisplayName("Verifies that the 'issued at' time is close to the generation time")
	void Given_MemberId_When_GenerateAccessToken_Then_IssuedAtClaimIsCloseToCurrentTime() {
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
	@DisplayName("Verifies that the expiration time is correct relative to the issued at time")
	void Given_MemberId_When_GenerateAccessToken_Then_ExpirationIsCorrect() {
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
	@DisplayName("Decodes the header from a valid token successfully")
	void Given_ValidToken_When_GetDecodedHeader_Then_ReturnsHeaderMap() {
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
	@DisplayName("Gets Authentication object from a valid token")
	void Given_ValidToken_When_GetAuthentication_Then_ReturnsAuthenticationObject() {
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
	@DisplayName("Throws ServerException when a token is expired")
	void Given_ExpiredToken_When_GetMemberIdFromToken_Then_ThrowsServerException() {
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
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.EXPIRED_TOKEN);
	}

	@Test
	@DisplayName("Throws ServerException when token structure is invalid")
	void Given_MalformedToken_When_GetMemberIdFromToken_Then_ThrowsServerException() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		final String malformedToken = "this.is.not.a.jwt";

		// when & then
		assertThatThrownBy(() -> jwtProvider.getMemberIdFromToken(malformedToken))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.MALFORMED_TOKEN);
	}

	@Test
	@DisplayName("Throws ServerException when token signature is invalid")
	void Given_TokenWithInvalidSignature_When_GetMemberIdFromToken_Then_ThrowsServerException() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		final SecretKey anotherKey = Jwts.SIG.HS256.key().build();
		final String token = Jwts.builder().subject("1").signWith(anotherKey).compact();

		// when & then
		assertThatThrownBy(() -> jwtProvider.getMemberIdFromToken(token))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_SIGNATURE);
	}

	@Test
	@DisplayName("Gets member ID from a valid token")
	void Given_ValidToken_When_GetMemberIdFromToken_Then_ReturnsCorrectMemberId() {
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
	@DisplayName("Throws ServerException when trying to get member ID from a non-expired access token")
	void Given_NonExpiredToken_When_GetMemberIdFromExpiredAccessToken_Then_ThrowsServerException() {
		// given
		doReturn(secretKey).when(jwtProperties).secretKey();
		doReturn(issuer).when(jwtProperties).issuer();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();

		final String token = jwtProvider.generateAccessToken(1L);

		// when & then
		assertThatThrownBy(() -> jwtProvider.getMemberIdFromExpiredAccessToken(token))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Gets member ID from an expired access token successfully")
	void Given_ExpiredToken_When_GetMemberIdFromExpiredAccessToken_Then_ReturnsCorrectMemberId() {
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
