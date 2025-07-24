package com.und.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private OidcClientFactory oidcClientFactory;

	@Mock
	private OidcProviderFactory oidcProviderFactory;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private NonceService nonceService;

	private final String providerId = "dummyId";
	private final String nickname = "dummyNickname";
	private final Long memberId = 1L;
	private final String idToken = "dummy.id.token";
	private final String accessToken = "dummy.access.token";
	private final String refreshToken = "dummy.refresh.token";
	private final Integer accessTokenExpireTime = 3600;
	private final Integer refreshTokenExpireTime = 7200;

	@Test
	@DisplayName("Throws an exception on handshake with an invalid provider")
	void Given_InvalidProvider_When_Handshake_Then_ThrowsException() {
		// given
		final HandshakeRequest handshakeRequest = new HandshakeRequest("facebook");

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.handshake(handshakeRequest));

		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Returns a nonce on a successful handshake")
	void Given_ValidProvider_When_Handshake_Then_ReturnsNonce() {
		// given
		final String nonce = "generated-nonce";
		final String providerName = "kakao";
		final HandshakeRequest handshakeRequest = new HandshakeRequest(providerName);

		doReturn(nonce).when(nonceService).generateNonceValue();
		doNothing().when(nonceService).saveNonce(nonce, Provider.KAKAO);

		// when
		final HandshakeResponse response = authService.handshake(handshakeRequest);

		// then
		verify(nonceService).generateNonceValue();
		verify(nonceService).saveNonce(nonce, Provider.KAKAO);
		assertThat(response.nonce()).isEqualTo(nonce);
	}

	@Test
	@DisplayName("Throws an exception on login with an invalid provider")
	void Given_InvalidProvider_When_Login_Then_ThrowsException() {
		// given
		final AuthRequest authRequest = new AuthRequest("facebook", idToken);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.login(authRequest));

		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Issues tokens successfully when a registered member logs in")
	void Given_RegisteredMember_When_Login_Then_IssuesTokensSuccessfully() {
		// given
		final AuthRequest authRequest = new AuthRequest("kakao", idToken);
		final OidcClient oidcClient = mock(OidcClient.class);
		final OidcPublicKeys keys = mock(OidcPublicKeys.class);
		final IdTokenPayload payload = new IdTokenPayload(providerId, nickname);
		final Member member = Member.builder().id(memberId).kakaoId(providerId).build();

		doReturn("nonce").when(jwtProvider).extractNonce(idToken);
		doNothing().when(nonceService).validateNonce("nonce", Provider.KAKAO);
		doReturn(oidcClient).when(oidcClientFactory).getOidcClient(Provider.KAKAO);
		doReturn(keys).when(oidcClient).getOidcPublicKeys();
		doReturn(payload).when(oidcProviderFactory).getIdTokenPayload(Provider.KAKAO, idToken, keys);
		doReturn(Optional.of(member)).when(memberRepository).findByKakaoId(providerId);
		setupTokenIssuance(accessToken, refreshToken);

		// when
		final AuthResponse response = authService.login(authRequest);

		// then
		verify(nonceService).validateNonce("nonce", Provider.KAKAO);
		verify(memberRepository, never()).save(any(Member.class));
		verify(refreshTokenService).saveRefreshToken(memberId, refreshToken);
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.accessToken()).isEqualTo(accessToken);
		assertThat(response.refreshToken()).isEqualTo(refreshToken);
		assertThat(response.accessTokenExpiresIn()).isEqualTo(accessTokenExpireTime);
		assertThat(response.refreshTokenExpiresIn()).isEqualTo(refreshTokenExpireTime);
	}

	@Test
	@DisplayName("Creates a new member and issues tokens on the first login")
	void Given_NewMember_When_Login_Then_CreatesMemberAndIssuesTokens() {
		// given
		final AuthRequest authRequest = new AuthRequest("kakao", idToken);
		final OidcClient oidcClient = mock(OidcClient.class);
		final OidcPublicKeys keys = mock(OidcPublicKeys.class);
		final IdTokenPayload payload = new IdTokenPayload(providerId, nickname);
		final Member newMember = Member.builder().id(memberId).kakaoId(providerId).build();

		doReturn("nonce").when(jwtProvider).extractNonce(idToken);
		doReturn(oidcClient).when(oidcClientFactory).getOidcClient(Provider.KAKAO);
		doReturn(keys).when(oidcClient).getOidcPublicKeys();
		doReturn(payload).when(oidcProviderFactory).getIdTokenPayload(Provider.KAKAO, idToken, keys);
		doReturn(Optional.empty()).when(memberRepository).findByKakaoId(providerId);
		doReturn(newMember).when(memberRepository).save(any(Member.class));
		setupTokenIssuance(accessToken, refreshToken);

		// when
		final AuthResponse response = authService.login(authRequest);

		// then
		verify(nonceService).validateNonce("nonce", Provider.KAKAO);
		verify(memberRepository).save(any(Member.class));
		verify(refreshTokenService).saveRefreshToken(memberId, refreshToken);
		assertThat(response.accessToken()).isEqualTo(accessToken);
		assertThat(response.refreshToken()).isEqualTo(refreshToken);
	}

	@Test
	@DisplayName("Throws an exception when reissuing tokens with a mismatched refresh token")
	void Given_MismatchedRefreshToken_When_ReissueTokens_Then_ThrowsException() {
		// given
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, "wrong.refresh.token");

		doReturn(memberId).when(jwtProvider).getMemberIdFromExpiredAccessToken(accessToken);
		doReturn(refreshToken).when(refreshTokenService).getRefreshToken(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.reissueTokens(request));

		verify(refreshTokenService).deleteRefreshToken(memberId);
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Throws an exception on token reissue if no refresh token is stored")
	void Given_NoStoredRefreshToken_When_ReissueTokens_Then_ThrowsException() {
		// given
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, refreshToken);

		doReturn(memberId).when(jwtProvider).getMemberIdFromExpiredAccessToken(accessToken);
		doReturn(null).when(refreshTokenService).getRefreshToken(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.reissueTokens(request));

		verify(refreshTokenService).deleteRefreshToken(memberId);
		verify(jwtProvider, never()).generateAccessToken(any());
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Reissues tokens successfully with a valid refresh token")
	void Given_ValidRefreshToken_When_ReissueTokens_Then_Succeeds() {
		// given
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, refreshToken);
		final String newAccessToken = "new-access-token";
		final String newRefreshToken = "new-refresh-token";

		doReturn(memberId).when(jwtProvider).getMemberIdFromExpiredAccessToken(accessToken);
		doReturn(refreshToken).when(refreshTokenService).getRefreshToken(memberId);
		setupTokenIssuance(newAccessToken, newRefreshToken);

		// when
		final AuthResponse response = authService.reissueTokens(request);

		// then
		verify(refreshTokenService).saveRefreshToken(memberId, newRefreshToken);
		assertThat(response.accessToken()).isEqualTo(newAccessToken);
		assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
		assertThat(response.accessTokenExpiresIn()).isEqualTo(accessTokenExpireTime);
		assertThat(response.refreshTokenExpiresIn()).isEqualTo(refreshTokenExpireTime);
	}

	private void setupTokenIssuance(final String newAccessToken, final String newRefreshToken) {
		doReturn(newAccessToken).when(jwtProvider).generateAccessToken(memberId);
		doReturn(newRefreshToken).when(refreshTokenService).generateRefreshToken();
		doReturn("Bearer").when(jwtProperties).type();
		doReturn(accessTokenExpireTime).when(jwtProperties).accessTokenExpireTime();
		doReturn(refreshTokenExpireTime).when(jwtProperties).refreshTokenExpireTime();
	}

}
