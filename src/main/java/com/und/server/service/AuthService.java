package com.und.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.dto.AuthRequest;
import com.und.server.dto.AuthResponse;
import com.und.server.dto.NonceRequest;
import com.und.server.dto.NonceResponse;
import com.und.server.dto.OidcPublicKeys;
import com.und.server.dto.RefreshTokenRequest;
import com.und.server.dto.TestAuthRequest;
import com.und.server.entity.Member;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.jwt.JwtProperties;
import com.und.server.jwt.JwtProvider;
import com.und.server.jwt.ParsedTokenInfo;
import com.und.server.oauth.IdTokenPayload;
import com.und.server.oauth.OidcClient;
import com.und.server.oauth.OidcClientFactory;
import com.und.server.oauth.OidcProviderFactory;
import com.und.server.oauth.Provider;
import com.und.server.util.ProfileManager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final MemberService memberService;
	private final OidcClientFactory oidcClientFactory;
	private final OidcProviderFactory oidcProviderFactory;
	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final NonceService nonceService;
	private final RefreshTokenService refreshTokenService;
	private final ProfileManager profileManager;

	// FIXME: Remove this method when deleting TestController
	@Transactional
	public AuthResponse issueTokensForTest(final TestAuthRequest request) {
		final Provider provider = convertToProvider(request.provider());
		final IdTokenPayload idTokenPayload = new IdTokenPayload(request.providerId(), request.nickname());
		final Member member = memberService.findOrCreateMember(provider, idTokenPayload);

		return issueTokens(member.getId());
	}

	@Transactional
	public NonceResponse handshake(final NonceRequest nonceRequest) {
		final String nonce = nonceService.generateNonceValue();
		final Provider provider = convertToProvider(nonceRequest.provider());

		nonceService.saveNonce(nonce, provider);

		return new NonceResponse(nonce);
	}

	@Transactional
	public AuthResponse login(final AuthRequest authRequest) {
		final Provider provider = convertToProvider(authRequest.provider());
		final IdTokenPayload idTokenPayload = validateIdTokenAndGetPayload(provider, authRequest.idToken());
		final Member member = memberService.findOrCreateMember(provider, idTokenPayload);

		return issueTokens(member.getId());
	}

	@Transactional
	public AuthResponse reissueTokens(final RefreshTokenRequest refreshTokenRequest) {
		final String accessToken = refreshTokenRequest.accessToken();
		final String providedRefreshToken = refreshTokenRequest.refreshToken();

		final Long memberId = getMemberIdForReissue(accessToken);

		memberService.findById(memberId).orElseThrow(() -> {
			refreshTokenService.deleteRefreshToken(memberId);
			return new ServerException(ServerErrorResult.INVALID_TOKEN);
		});

		refreshTokenService.validateRefreshToken(memberId, providedRefreshToken);

		return issueTokens(memberId);
	}

	@Transactional
	public void logout(final Long memberId) {
		refreshTokenService.deleteRefreshToken(memberId);
	}

	private Provider convertToProvider(final String providerName) {
		try {
			return Provider.valueOf(providerName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ServerException(ServerErrorResult.INVALID_PROVIDER);
		}
	}

	private IdTokenPayload validateIdTokenAndGetPayload(final Provider provider, final String idToken) {
		final String nonce = jwtProvider.extractNonce(idToken);
		nonceService.validateNonce(nonce, provider);

		final OidcClient oidcClient = oidcClientFactory.getOidcClient(provider);
		final OidcPublicKeys oidcPublicKeys = oidcClient.getOidcPublicKeys();

		return oidcProviderFactory.getIdTokenPayload(provider, idToken, oidcPublicKeys);
	}

	private AuthResponse issueTokens(final Long memberId) {
		final String accessToken = jwtProvider.generateAccessToken(memberId);
		final String refreshToken = refreshTokenService.generateRefreshToken();
		refreshTokenService.saveRefreshToken(memberId, refreshToken);

		return new AuthResponse(
			jwtProperties.type(),
			accessToken,
			jwtProperties.accessTokenExpireTime(),
			refreshToken,
			jwtProperties.refreshTokenExpireTime());
	}

	private Long getMemberIdForReissue(final String accessToken) {
		final ParsedTokenInfo tokenInfo = jwtProvider.parseTokenForReissue(accessToken);
		final Long memberId = tokenInfo.memberId();

		if (!tokenInfo.isExpired()) {
			// An attempt to reissue with a non-expired token may be a security risk.
			// For security, we delete the refresh token.
			refreshTokenService.deleteRefreshToken(memberId);
			if (profileManager.isProdOrStgProfile()) {
				throw new ServerException(ServerErrorResult.INVALID_TOKEN);
			}
			throw new ServerException(ServerErrorResult.NOT_EXPIRED_TOKEN);
		}

		return memberId;
	}

}
