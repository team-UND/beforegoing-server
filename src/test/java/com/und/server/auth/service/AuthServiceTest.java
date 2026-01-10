package com.und.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.und.server.member.dto.MemberCreationResult;
import com.und.server.member.entity.Member;
import com.und.server.member.exception.MemberErrorResult;
import com.und.server.member.service.MemberService;

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
	private NonceService nonceService;
	@Mock
	private RefreshTokenService refreshTokenService;
	@Mock
	private ProfileManager profileManager;

	@Test
	@DisplayName("Issue tokens for test user successfully")
	void Given_ValidTestAuthRequest_When_IssueTokensForTest_Then_ReturnsAuthResponse() {
		// given
		TestAuthRequest request = new TestAuthRequest("kakao", "12345");
		Member member = Member.builder().id(1L).build();
		MemberCreationResult creationResult = new MemberCreationResult(member, true);

		doReturn(creationResult).when(memberService).findOrCreateMember(Provider.KAKAO, "12345");
		doReturn("access-token").when(jwtProvider).generateAccessToken(1L);
		doReturn("refresh-token").when(refreshTokenService).generateRefreshToken();
		doReturn("Bearer").when(jwtProperties).type();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();
		doReturn(1209600).when(jwtProperties).refreshTokenExpireTime();

		// when
		AuthResponse response = authService.issueTokensForTest(request);

		// then
		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
		assertThat(response.isNewMember()).isTrue();
		verify(refreshTokenService).saveRefreshToken(1L, "refresh-token");
	}

	@Test
	@DisplayName("Generate nonce successfully")
	void Given_ValidNonceRequest_When_GenerateNonce_Then_ReturnsNonceResponse() {
		// given
		NonceRequest request = new NonceRequest("kakao");
		doReturn("nonce-value").when(nonceService).generateNonceValue();

		// when
		NonceResponse response = authService.generateNonce(request);

		// then
		assertThat(response.nonce()).isEqualTo("nonce-value");
		verify(nonceService).saveNonce("nonce-value", Provider.KAKAO);
	}

	@Test
	@DisplayName("Generate nonce with invalid provider throws exception")
	void Given_InvalidProvider_When_GenerateNonce_Then_ThrowsException() {
		// given
		NonceRequest request = new NonceRequest("invalid");

		// when & then
		assertThatThrownBy(() -> authService.generateNonce(request))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(AuthErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Login successfully")
	void Given_ValidLoginRequest_When_Login_Then_ReturnsAuthResponse() {
		// given
		AuthRequest request = new AuthRequest("kakao", "id-token");
		Member member = Member.builder().id(1L).build();
		MemberCreationResult creationResult = new MemberCreationResult(member, false);
		OidcClient oidcClient = mock(OidcClient.class);
		OidcPublicKeys publicKeys = mock(OidcPublicKeys.class);

		doReturn("nonce").when(jwtProvider).extractNonce("id-token");
		doReturn(oidcClient).when(oidcClientFactory).getOidcClient(Provider.KAKAO);
		doReturn(publicKeys).when(oidcClient).getOidcPublicKeys();
		doReturn("provider-id").when(oidcProviderFactory).getProviderId(Provider.KAKAO, "id-token", publicKeys);
		doReturn(creationResult).when(memberService).findOrCreateMember(Provider.KAKAO, "provider-id");

		doReturn("access-token").when(jwtProvider).generateAccessToken(1L);
		doReturn("refresh-token").when(refreshTokenService).generateRefreshToken();
		doReturn("Bearer").when(jwtProperties).type();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();
		doReturn(1209600).when(jwtProperties).refreshTokenExpireTime();

		// when
		AuthResponse response = authService.login(request);

		// then
		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.isNewMember()).isFalse();
		verify(nonceService).verifyNonce("nonce", Provider.KAKAO);
	}

	@Test
	@DisplayName("Reissue tokens successfully")
	void Given_ValidRefreshTokenRequest_When_ReissueTokens_Then_ReturnsNewTokens() {
		// given
		RefreshTokenRequest request = new RefreshTokenRequest("expired-access", "valid-refresh");
		ParsedTokenInfo tokenInfo = new ParsedTokenInfo(1L, true); // expired

		doReturn(tokenInfo).when(jwtProvider).parseTokenForReissue("expired-access");
		// checkMemberExists returns void, so no doReturn needed unless throwing

		doReturn("new-access").when(jwtProvider).generateAccessToken(1L);
		doReturn("new-refresh").when(refreshTokenService).generateRefreshToken();
		doReturn("Bearer").when(jwtProperties).type();
		doReturn(3600).when(jwtProperties).accessTokenExpireTime();
		doReturn(1209600).when(jwtProperties).refreshTokenExpireTime();

		// when
		AuthResponse response = authService.reissueTokens(request);

		// then
		assertThat(response.accessToken()).isEqualTo("new-access");
		assertThat(response.isNewMember()).isFalse();
		verify(refreshTokenService).verifyRefreshToken(1L, "valid-refresh");
	}

	@Test
	@DisplayName("Reissue tokens fails when member not found")
	void Given_MemberNotFound_When_ReissueTokens_Then_ThrowsInvalidTokenException() {
		// given
		RefreshTokenRequest request = new RefreshTokenRequest("expired-access", "refresh");
		ParsedTokenInfo tokenInfo = new ParsedTokenInfo(1L, true);

		doReturn(tokenInfo).when(jwtProvider).parseTokenForReissue("expired-access");
		doThrow(new ServerException(MemberErrorResult.MEMBER_NOT_FOUND))
			.when(memberService).checkMemberExists(1L);

		// when & then
		assertThatThrownBy(() -> authService.reissueTokens(request))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(AuthErrorResult.INVALID_TOKEN);

		verify(refreshTokenService).deleteRefreshToken(1L);
	}

	@Test
	@DisplayName("Reissue tokens fails when other member error occurs")
	void Given_InvalidMemberId_When_ReissueTokens_Then_ThrowsInvalidTokenException() {
		// given
		RefreshTokenRequest request = new RefreshTokenRequest("expired-access", "refresh");
		ParsedTokenInfo tokenInfo = new ParsedTokenInfo(1L, true);

		doReturn(tokenInfo).when(jwtProvider).parseTokenForReissue("expired-access");
		doThrow(new ServerException(MemberErrorResult.INVALID_MEMBER_ID))
			.when(memberService).checkMemberExists(1L);

		// when & then
		assertThatThrownBy(() -> authService.reissueTokens(request))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(AuthErrorResult.INVALID_TOKEN);

		verify(refreshTokenService, never()).deleteRefreshToken(1L);
	}

	@Test
	@DisplayName("Reissue tokens fails when access token is not expired (Prod)")
	void Given_NotExpiredTokenAndProdProfile_When_ReissueTokens_Then_ThrowsInvalidTokenException() {
		// given
		RefreshTokenRequest request = new RefreshTokenRequest("valid-access", "refresh");
		ParsedTokenInfo tokenInfo = new ParsedTokenInfo(1L, false); // not expired

		doReturn(tokenInfo).when(jwtProvider).parseTokenForReissue("valid-access");
		doReturn(true).when(profileManager).isProdOrStgProfile();

		// when & then
		assertThatThrownBy(() -> authService.reissueTokens(request))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(AuthErrorResult.INVALID_TOKEN);

		verify(refreshTokenService).deleteRefreshToken(1L);
	}

	@Test
	@DisplayName("Reissue tokens fails when access token is not expired (Local)")
	void Given_NotExpiredTokenAndLocalProfile_When_ReissueTokens_Then_ThrowsNotExpiredTokenException() {
		// given
		RefreshTokenRequest request = new RefreshTokenRequest("valid-access", "refresh");
		ParsedTokenInfo tokenInfo = new ParsedTokenInfo(1L, false); // not expired

		doReturn(tokenInfo).when(jwtProvider).parseTokenForReissue("valid-access");
		doReturn(false).when(profileManager).isProdOrStgProfile();

		// when & then
		assertThatThrownBy(() -> authService.reissueTokens(request))
			.isInstanceOf(ServerException.class)
			.extracting("errorResult")
			.isEqualTo(AuthErrorResult.NOT_EXPIRED_TOKEN);

		verify(refreshTokenService).deleteRefreshToken(1L);
	}

	@Test
	@DisplayName("Logout successfully")
	void Given_MemberId_When_Logout_Then_DeletesRefreshToken() {
		// when
		authService.logout(1L);

		// then
		verify(refreshTokenService).deleteRefreshToken(1L);
	}
}
