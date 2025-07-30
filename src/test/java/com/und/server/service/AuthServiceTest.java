package com.und.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private MemberService memberService;

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

	@Mock
	private ProfileManager profileManager;

	private final String providerId = "dummyId";
	private final String nickname = "dummyNickname";
	private final Long memberId = 1L;
	private final String idToken = "dummy.id.token";
	private final String accessToken = "dummy.access.token";
	private final String refreshToken = "dummy.refresh.token";
	private final Integer accessTokenExpireTime = 3600;
	private final Integer refreshTokenExpireTime = 7200;

	// FIXME: Remove this test method when deleting TestController
	@Test
	@DisplayName("Issues tokens for an existing member for testing purposes")
	void Given_ExistingMemberForTest_When_IssueTokensForTest_Then_Succeeds() {
		// given
		final TestAuthRequest request = new TestAuthRequest("kakao", providerId, nickname);
		final Member existingMember = Member.builder().id(memberId).kakaoId(providerId).nickname(nickname).build();
		doReturn(existingMember).when(memberService).findOrCreateMember(any(Provider.class), any(IdTokenPayload.class));
		setupTokenIssuance(accessToken, refreshToken);

		// when
		final AuthResponse response = authService.issueTokensForTest(request);

		// then
		verify(memberService).findOrCreateMember(any(Provider.class), any(IdTokenPayload.class));
		verify(refreshTokenService).saveRefreshToken(memberId, refreshToken);
		assertThat(response.accessToken()).isEqualTo(accessToken);
		assertThat(response.refreshToken()).isEqualTo(refreshToken);
	}

	// FIXME: Remove this test method when deleting TestController
	@Test
	@DisplayName("Creates a new member and issues tokens for testing purposes")
	void Given_NewMemberForTest_When_IssueTokensForTest_Then_CreatesMemberAndSucceeds() {
		// given
		final TestAuthRequest request = new TestAuthRequest("kakao", providerId, nickname);
		final Member newMember = Member.builder().id(memberId).kakaoId(providerId).nickname(nickname).build();
		doReturn(newMember).when(memberService).findOrCreateMember(any(Provider.class), any(IdTokenPayload.class));
		setupTokenIssuance(accessToken, refreshToken);

		// when
		final AuthResponse response = authService.issueTokensForTest(request);

		// then
		verify(memberService).findOrCreateMember(any(Provider.class), any(IdTokenPayload.class));
		verify(refreshTokenService).saveRefreshToken(memberId, refreshToken);
		assertThat(response.accessToken()).isEqualTo(accessToken);
		assertThat(response.refreshToken()).isEqualTo(refreshToken);
	}

	@Test
	@DisplayName("Throws an exception on handshake with an invalid provider")
	void Given_InvalidProvider_When_Handshake_Then_ThrowsException() {
		// given
		final NonceRequest nonceRequest = new NonceRequest("facebook");

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.handshake(nonceRequest));

		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Returns a nonce on a successful handshake")
	void Given_ValidProvider_When_Handshake_Then_ReturnsNonce() {
		// given
		final String nonce = "generated-nonce";
		final String providerName = "kakao";
		final NonceRequest nonceRequest = new NonceRequest(providerName);

		doReturn(nonce).when(nonceService).generateNonceValue();
		doNothing().when(nonceService).saveNonce(nonce, Provider.KAKAO);

		// when
		final NonceResponse response = authService.handshake(nonceRequest);

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
		doReturn(member).when(memberService).findOrCreateMember(Provider.KAKAO, payload);
		setupTokenIssuance(accessToken, refreshToken);

		// when
		final AuthResponse response = authService.login(authRequest);

		// then
		verify(nonceService).validateNonce("nonce", Provider.KAKAO);
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
		doReturn(newMember).when(memberService).findOrCreateMember(Provider.KAKAO, payload);
		setupTokenIssuance(accessToken, refreshToken);

		// when
		final AuthResponse response = authService.login(authRequest);

		// then
		verify(nonceService).validateNonce("nonce", Provider.KAKAO);
		verify(refreshTokenService).saveRefreshToken(memberId, refreshToken);
		assertThat(response.accessToken()).isEqualTo(accessToken);
		assertThat(response.refreshToken()).isEqualTo(refreshToken);
	}

	@Test
	@DisplayName("Throws an exception on token reissue if the member does not exist")
	void Given_NonExistentMember_When_ReissueTokens_Then_ThrowsExceptionAndDeletesToken() {
		// given
		final ParsedTokenInfo expiredTokenInfo = new ParsedTokenInfo(memberId, true);
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, refreshToken);

		doReturn(expiredTokenInfo).when(jwtProvider).parseTokenForReissue(accessToken);
		doReturn(Optional.empty()).when(memberService).findById(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.reissueTokens(request));

		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_TOKEN);
		verify(refreshTokenService).deleteRefreshToken(memberId);
		verify(refreshTokenService, never()).validateRefreshToken(any(), any());
	}

	@Test
	@DisplayName("Throws an exception when reissuing tokens with a mismatched refresh token")
	void Given_MismatchedRefreshToken_When_ReissueTokens_Then_ThrowsException() {
		// given
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, "wrong.refresh.token");
		final ParsedTokenInfo expiredTokenInfo = new ParsedTokenInfo(memberId, true);
		final Member member = Member.builder().id(memberId).build();

		doReturn(expiredTokenInfo).when(jwtProvider).parseTokenForReissue(accessToken);
		doReturn(Optional.of(member)).when(memberService).findById(memberId);
		doThrow(new ServerException(ServerErrorResult.INVALID_TOKEN))
			.when(refreshTokenService).validateRefreshToken(memberId, "wrong.refresh.token");

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.reissueTokens(request));

		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Throws an exception on token reissue if no refresh token is stored")
	void Given_NoStoredRefreshToken_When_ReissueTokens_Then_ThrowsException() {
		// given
		final ParsedTokenInfo expiredTokenInfo = new ParsedTokenInfo(memberId, true);
		final Member member = Member.builder().id(memberId).build();
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, refreshToken);

		doReturn(expiredTokenInfo).when(jwtProvider).parseTokenForReissue(accessToken);
		doReturn(Optional.of(member)).when(memberService).findById(memberId);
		doThrow(new ServerException(ServerErrorResult.INVALID_TOKEN))
			.when(refreshTokenService).validateRefreshToken(memberId, refreshToken);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.reissueTokens(request));

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
		final ParsedTokenInfo expiredTokenInfo = new ParsedTokenInfo(memberId, true);
		final Member member = Member.builder().id(memberId).build();

		doReturn(expiredTokenInfo).when(jwtProvider).parseTokenForReissue(accessToken);
		doReturn(Optional.of(member)).when(memberService).findById(memberId);
		doNothing().when(refreshTokenService).validateRefreshToken(memberId, refreshToken);
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

	@Test
	@DisplayName("Throws INVALID_TOKEN and deletes refresh token for a non-expired token on prod/stg profiles")
	void Given_NonExpiredTokenOnProd_When_ReissueTokens_Then_ThrowsInvalidToken() {
		// given
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, refreshToken);
		final ParsedTokenInfo nonExpiredTokenInfo = new ParsedTokenInfo(memberId, false);

		doReturn(nonExpiredTokenInfo).when(jwtProvider).parseTokenForReissue(accessToken);
		doReturn(true).when(profileManager).isProdOrStgProfile();

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.reissueTokens(request));

		// then
		verify(refreshTokenService).deleteRefreshToken(memberId);
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Throws NOT_EXPIRED_TOKEN and deletes refresh token for a non-expired token on dev/local profiles")
	void Given_NonExpiredTokenOnDev_When_ReissueTokens_Then_ThrowsNotExpiredToken() {
		// given
		final RefreshTokenRequest request = new RefreshTokenRequest(accessToken, refreshToken);
		final ParsedTokenInfo nonExpiredTokenInfo = new ParsedTokenInfo(memberId, false);

		doReturn(nonExpiredTokenInfo).when(jwtProvider).parseTokenForReissue(accessToken);
		doReturn(false).when(profileManager).isProdOrStgProfile();

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> authService.reissueTokens(request));

		// then
		verify(refreshTokenService).deleteRefreshToken(memberId);
		assertThat(exception.getErrorResult()).isEqualTo(ServerErrorResult.NOT_EXPIRED_TOKEN);
	}

	@Test
	@DisplayName("Deletes refresh token on logout")
	void Given_MemberId_When_Logout_Then_DeletesRefreshToken() {
		// given
		final Long memberId = 1L;

		// when
		authService.logout(memberId);

		// then
		verify(refreshTokenService).deleteRefreshToken(memberId);
	}

	private void setupTokenIssuance(final String newAccessToken, final String newRefreshToken) {
		doReturn(newAccessToken).when(jwtProvider).generateAccessToken(memberId);
		doReturn(newRefreshToken).when(refreshTokenService).generateRefreshToken();
		doReturn("Bearer").when(jwtProperties).type();
		doReturn(accessTokenExpireTime).when(jwtProperties).accessTokenExpireTime();
		doReturn(refreshTokenExpireTime).when(jwtProperties).refreshTokenExpireTime();
	}

}
