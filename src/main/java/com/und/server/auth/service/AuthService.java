package com.und.server.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.auth.dto.OidcPublicKeys;
import com.und.server.auth.dto.request.AuthRequest;
import com.und.server.auth.dto.request.NonceRequest;
import com.und.server.auth.dto.request.RefreshTokenRequest;
import com.und.server.auth.dto.response.AuthResponse;
import com.und.server.auth.dto.response.NonceResponse;
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.jwt.JwtProperties;
import com.und.server.auth.jwt.JwtProvider;
import com.und.server.auth.jwt.ParsedTokenInfo;
import com.und.server.auth.oauth.OidcClient;
import com.und.server.auth.oauth.OidcClientFactory;
import com.und.server.auth.oauth.OidcProviderFactory;
import com.und.server.auth.oauth.Provider;
import com.und.server.common.dto.request.TestAuthRequest;
import com.und.server.common.exception.ServerException;
import com.und.server.common.util.ProfileManager;
import com.und.server.member.entity.Member;
import com.und.server.member.exception.MemberErrorResult;
import com.und.server.member.service.MemberService;

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

	@Transactional
	public AuthResponse issueTokensForTest(final TestAuthRequest request) {
		final Provider provider = convertToProvider(request.provider());
		final String providerId = request.providerId();
		final Member member = memberService.findOrCreateMember(provider, providerId);

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
		final String idToken = authRequest.idToken();

		verifyIdTokenNonce(provider, idToken);
		final String providerId = getProviderIdFromIdToken(provider, idToken);
		final Member member = memberService.findOrCreateMember(provider, providerId);

		return issueTokens(member.getId());
	}

	@Transactional
	public AuthResponse reissueTokens(final RefreshTokenRequest refreshTokenRequest) {
		final String accessToken = refreshTokenRequest.accessToken();
		final String providedRefreshToken = refreshTokenRequest.refreshToken();

		final Long memberId = getMemberIdForReissue(accessToken);

		try {
			memberService.checkMemberExists(memberId);
		} catch (final ServerException e) {
			if (e.getErrorResult() == MemberErrorResult.MEMBER_NOT_FOUND) {
				// The member ID is not null, but the member doesn't exist.
				// This is a security concern, so delete the orphaned refresh token.
				refreshTokenService.deleteRefreshToken(memberId);
			}
			// For both MEMBER_NOT_FOUND and INVALID_MEMBER_ID, treat it as an invalid token situation.
			throw new ServerException(AuthErrorResult.INVALID_TOKEN, e);
		}

		refreshTokenService.verifyRefreshToken(memberId, providedRefreshToken);

		return issueTokens(memberId);
	}

	@Transactional
	public void logout(final Long memberId) {
		refreshTokenService.deleteRefreshToken(memberId);
	}

	private Provider convertToProvider(final String providerName) {
		try {
			return Provider.valueOf(providerName.toUpperCase());
		} catch (final IllegalArgumentException e) {
			throw new ServerException(AuthErrorResult.INVALID_PROVIDER);
		}
	}

	private void verifyIdTokenNonce(final Provider provider, final String idToken) {
		final String nonce = jwtProvider.extractNonce(idToken);
		nonceService.verifyNonce(nonce, provider);
	}

	private String getProviderIdFromIdToken(final Provider provider, final String idToken) {
		final OidcClient oidcClient = oidcClientFactory.getOidcClient(provider);
		final OidcPublicKeys oidcPublicKeys = oidcClient.getOidcPublicKeys();

		return oidcProviderFactory.getProviderId(provider, idToken, oidcPublicKeys);
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
			// For security, delete the refresh token.
			refreshTokenService.deleteRefreshToken(memberId);
			if (profileManager.isProdOrStgProfile()) {
				throw new ServerException(AuthErrorResult.INVALID_TOKEN);
			}
			throw new ServerException(AuthErrorResult.NOT_EXPIRED_TOKEN);
		}

		return memberId;
	}

}
