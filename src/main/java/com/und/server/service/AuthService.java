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
		final String providerId = oidcProviderFactory.getOidcProviderId(provider, idToken, oidcPublicKeys);

		Member member = findMemberByProviderId(provider, providerId);
		if (member == null) {
			member = createMember(provider, providerId);
		}

		final String accessToken = jwtProvider.generateAccessToken(member.getId());
		final String refreshToken = refreshTokenService.generateRefreshToken();

		refreshTokenService.saveRefreshToken(member.getId(), refreshToken);

		return new AuthResponse(
			jwtProperties.type(),
			accessToken,
			jwtProperties.accessTokenExpireTime(),
			refreshToken,
			jwtProperties.refreshTokenExpireTime()
		);
	}

	private Member findMemberByProviderId(final Provider provider, final String providerId) {
		return switch (provider) {
			case KAKAO -> memberRepository.findByKakaoId(providerId).orElse(null);
			// Add extra providers
			default -> throw new ServerException(ServerErrorResult.INVALID_PROVIDER);
		};
	}

	private Member createMember(final Provider provider, final String providerId) {
		Member newMember = Member.builder()
			.kakaoId(provider == Provider.KAKAO ? providerId : null)
			// Add extra providers
			.build();

		return memberRepository.save(newMember);
	}

	@Transactional
	public AuthResponse reissueAccessToken(final RefreshTokenRequest refreshTokenRequest) {
		final String accessToken = refreshTokenRequest.accessToken();
		final String requestRefreshToken = refreshTokenRequest.refreshToken();

		Long memberId;
		try {
			memberId = jwtProvider.getMemberIdFromExpiredAccessToken(accessToken);
		} catch (Exception e) {
			throw new ServerException(ServerErrorResult.INVALID_TOKEN, e);
		}

		final String savedRefreshToken = refreshTokenService.getRefreshToken(memberId);
		if (!requestRefreshToken.equals(savedRefreshToken)) {
			refreshTokenService.deleteRefreshToken(memberId);
			throw new ServerException(ServerErrorResult.INVALID_TOKEN);
		}

		final String newAccessToken = jwtProvider.generateAccessToken(memberId);
		final String newRefreshToken = refreshTokenService.generateRefreshToken();
		refreshTokenService.saveRefreshToken(memberId, newRefreshToken);

		return new AuthResponse(
			jwtProperties.type(),
			newAccessToken,
			jwtProperties.accessTokenExpireTime(),
			newRefreshToken,
			jwtProperties.refreshTokenExpireTime()
		);
	}

}
