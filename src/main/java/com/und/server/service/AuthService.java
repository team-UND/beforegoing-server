package com.und.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.dto.AuthRequest;
import com.und.server.dto.AuthResponse;
import com.und.server.dto.HandshakeRequest;
import com.und.server.dto.HandshakeResponse;
import com.und.server.dto.OidcPublicKeys;
import com.und.server.dto.RefreshTokenRequest;
import com.und.server.entity.Member;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.jwt.JwtProperties;
import com.und.server.jwt.JwtProvider;
import com.und.server.oauth.IdTokenPayload;
import com.und.server.oauth.OidcClient;
import com.und.server.oauth.OidcClientFactory;
import com.und.server.oauth.OidcProviderFactory;
import com.und.server.oauth.Provider;
import com.und.server.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final MemberRepository memberRepository;
	private final OidcClientFactory oidcClientFactory;
	private final OidcProviderFactory oidcProviderFactory;
	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final NonceService nonceService;
	private final RefreshTokenService refreshTokenService;

	@Transactional
	public HandshakeResponse handshake(final HandshakeRequest handshakeRequest) {
		final String nonce = nonceService.generateNonceValue();
		final Provider provider = handshakeRequest.provider();

		nonceService.saveNonce(nonce, provider);

		return new HandshakeResponse(nonce);
	}

	@Transactional
	public AuthResponse login(final AuthRequest authRequest) {
		final Provider provider = authRequest.provider();
		final String idToken = authRequest.idToken();

		final String nonce = jwtProvider.extractNonce(idToken);
		nonceService.validateNonce(nonce, provider);

		final OidcClient oidcClient = oidcClientFactory.getOidcClient(provider);
		final OidcPublicKeys oidcPublicKeys = oidcClient.getOidcPublicKeys();
		final IdTokenPayload idTokenPayload = oidcProviderFactory.getIdTokenPayload(provider, idToken, oidcPublicKeys);
		final String providerId = idTokenPayload.providerId();
		final String nickname = idTokenPayload.nickname();

		Member member = findMemberByProviderId(provider, providerId);
		if (member == null) {
			member = createMember(provider, providerId, nickname);
		}

		return issueTokens(member.getId());
	}

	@Transactional
	public AuthResponse reissueTokens(final RefreshTokenRequest refreshTokenRequest) {
		final String accessToken = refreshTokenRequest.accessToken();
		final String providedRefreshToken = refreshTokenRequest.refreshToken();

		final Long memberId = jwtProvider.getMemberIdFromExpiredAccessToken(accessToken);
		final String savedRefreshToken = refreshTokenService.getRefreshToken(memberId);
		if (!providedRefreshToken.equals(savedRefreshToken)) {
			refreshTokenService.deleteRefreshToken(memberId);
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}

		return issueTokens(memberId);
	}

	private Member findMemberByProviderId(final Provider provider, final String providerId) {
		return switch (provider) {
			case KAKAO -> memberRepository.findByKakaoId(providerId).orElse(null);
			// Add extra providers
			default -> throw new ServerException(ServerErrorResult.INVALID_PROVIDER);
		};
	}

	private Member createMember(final Provider provider, final String providerId, final String nickname) {
		final Member newMember = Member.builder()
			.kakaoId(provider == Provider.KAKAO ? providerId : null)
			// Add extra providers
			.nickname(nickname)
			.build();

		return memberRepository.save(newMember);
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

}
