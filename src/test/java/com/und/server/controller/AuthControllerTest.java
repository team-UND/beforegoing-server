package com.und.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.dto.AuthRequest;
import com.und.server.dto.AuthResponse;
import com.und.server.dto.NonceRequest;
import com.und.server.dto.NonceResponse;
import com.und.server.dto.RefreshTokenRequest;
import com.und.server.exception.GlobalExceptionHandler;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@InjectMocks
	private AuthController authController;

	@Mock
	private AuthService authService;

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(authController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("Fails handshake with bad request when provider is null")
	void Given_HandshakeRequestWithNullProvider_When_Handshake_Then_ReturnsBadRequest() throws Exception {
		// given
		final String url = "/v1/auth/nonce";
		final NonceRequest request = new NonceRequest(null);
		final String requestBody = objectMapper.writeValueAsString(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ServerErrorResult.INVALID_PARAMETER.name()))
			.andExpect(jsonPath("$.message[0]").value("Provider must not be null"));
	}

	@Test
	@DisplayName("Fails handshake when provider is unknown")
	void Given_HandshakeRequestWithUnknownProvider_When_Handshake_Then_ReturnsErrorResponse() throws Exception {
		// given
		final String url = "/v1/auth/nonce";
		final NonceRequest request = new NonceRequest("facebook");
		final String requestBody = objectMapper.writeValueAsString(request);
		final ServerErrorResult errorResult = ServerErrorResult.INVALID_PROVIDER;

		doThrow(new ServerException(errorResult))
			.when(authService).handshake(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().is(errorResult.getHttpStatus().value()))
			.andExpect(jsonPath("$.code").value(errorResult.name()))
			.andExpect(jsonPath("$.message").value(errorResult.getMessage()));
	}

	@Test
	@DisplayName("Succeeds handshake and returns nonce for a valid request")
	void Given_ValidHandshakeRequest_When_Handshake_Then_ReturnsOkWithNonce() throws Exception {
		// given
		final String url = "/v1/auth/nonce";
		final NonceRequest request = new NonceRequest("kakao");
		final NonceResponse response = new NonceResponse("generated-nonce");

		doReturn(response).when(authService).handshake(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.nonce").value("generated-nonce"));
	}

	@Test
	@DisplayName("Fails login with bad request when provider is null")
	void Given_LoginRequestWithNullProvider_When_Login_Then_ReturnsBadRequest() throws Exception {
		// given
		final String url = "/v1/auth/login";
		final AuthRequest request = new AuthRequest(null, "dummy.id.token");
		final String requestBody = objectMapper.writeValueAsString(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ServerErrorResult.INVALID_PARAMETER.name()))
			.andExpect(jsonPath("$.message[0]").value("Provider must not be null"));
	}

	@Test
	@DisplayName("Fails login with bad request when ID token is null")
	void Given_LoginRequestWithNullIdToken_When_Login_Then_ReturnsBadRequest() throws Exception {
		// given
		final String url = "/v1/auth/login";
		final AuthRequest request = new AuthRequest("kakao", null);
		final String requestBody = objectMapper.writeValueAsString(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ServerErrorResult.INVALID_PARAMETER.name()))
			.andExpect(jsonPath("$.message[0]").value("ID Token must not be null"));
	}

	@Test
	@DisplayName("Fails login when provider is unknown")
	void Given_LoginRequestWithUnknownProvider_When_Login_Then_ReturnsErrorResponse() throws Exception {
		// given
		final String url = "/v1/auth/login";
		final AuthRequest request = new AuthRequest("facebook", "dummy.id.token");
		final String requestBody = objectMapper.writeValueAsString(request);
		final ServerErrorResult errorResult = ServerErrorResult.INVALID_PROVIDER;

		doThrow(new ServerException(errorResult))
			.when(authService).login(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().is(errorResult.getHttpStatus().value()))
			.andExpect(jsonPath("$.code").value(errorResult.name()))
			.andExpect(jsonPath("$.message").value(errorResult.getMessage()));
	}

	@Test
	@DisplayName("Fails login with internal server error for an unknown exception")
	void Given_LoginRequest_When_ServiceThrowsUnknownException_Then_ReturnsInternalServerError() throws Exception {
		// given
		final String url = "/v1/auth/login";
		final AuthRequest request = new AuthRequest("kakao", "dummy.id.token");
		final String requestBody = objectMapper.writeValueAsString(request);
		final ServerErrorResult errorResult = ServerErrorResult.UNKNOWN_EXCEPTION;

		doThrow(new RuntimeException("A wild unexpected error appeared!"))
			.when(authService).login(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(errorResult.name()))
			.andExpect(jsonPath("$.message").value(errorResult.getMessage()));
	}

	@Test
	@DisplayName("Succeeds login and issues tokens for a valid request")
	void Given_ValidLoginRequest_When_Login_Then_ReturnsOkWithTokens() throws Exception {
		// given
		final String url = "/v1/auth/login";
		final AuthRequest authRequest = new AuthRequest("kakao", "dummy.id.token");
		final AuthResponse authResponse = new AuthResponse(
			"Bearer",
			"dummy.access.token",
			10000,
			"dummy.refresh.token",
			20000
		);

		doReturn(authResponse).when(authService).login(authRequest);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(authRequest))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		final AuthResponse response = objectMapper.readValue(
			resultActions
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8), AuthResponse.class
		);

		resultActions.andExpect(status().isOk());
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.accessToken()).isEqualTo("dummy.access.token");
		assertThat(response.accessTokenExpiresIn()).isEqualTo(10000);
		assertThat(response.refreshToken()).isEqualTo("dummy.refresh.token");
		assertThat(response.refreshTokenExpiresIn()).isEqualTo(20000);
	}

	@Test
	@DisplayName("Fails token refresh with bad request when access token is null")
	void Given_RefreshTokenRequestWithNullAccessToken_When_ReissueTokens_Then_ReturnsBadRequest() throws Exception {
		// given
		final String url = "/v1/auth/tokens";
		final RefreshTokenRequest request = new RefreshTokenRequest(null, "old.refresh.token");
		final String requestBody = objectMapper.writeValueAsString(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ServerErrorResult.INVALID_PARAMETER.name()))
			.andExpect(jsonPath("$.message[0]").value("Access Token must not be null"));
	}

	@Test
	@DisplayName("Fails token refresh with bad request when refresh token is null")
	void Given_RefreshTokenRequestWithNullRefreshToken_When_ReissueTokens_Then_ReturnsBadRequest() throws Exception {
		// given
		final String url = "/v1/auth/tokens";
		final RefreshTokenRequest request = new RefreshTokenRequest("old.access.token", null);
		final String requestBody = objectMapper.writeValueAsString(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ServerErrorResult.INVALID_PARAMETER.name()))
			.andExpect(jsonPath("$.message[0]").value("Refresh Token must not be null"));
	}

	@Test
	@DisplayName("Succeeds token refresh for a valid request")
	void Given_ValidRefreshTokenRequest_When_ReissueTokens_Then_ReturnsOkWithNewTokens() throws Exception {
		// given
		final String url = "/v1/auth/tokens";
		final RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(
			"old.access.token", "old.refresh.token"
		);
		final AuthResponse authResponse = new AuthResponse(
			"Bearer",
			"new.access.token",
			10000,
			"new.refresh.token",
			7200
		);

		doReturn(authResponse).when(authService).reissueTokens(refreshTokenRequest);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(refreshTokenRequest))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		final AuthResponse response = objectMapper.readValue(
			resultActions
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8), AuthResponse.class
		);

		resultActions.andExpect(status().isOk());
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.accessToken()).isEqualTo("new.access.token");
		assertThat(response.refreshToken()).isEqualTo("new.refresh.token");
	}

	@Test
	@DisplayName("Succeeds logout and returns no content")
	void Given_AuthenticatedUser_When_Logout_Then_ReturnsNoContent() throws Exception {
		// given
		final String url = "/v1/auth/logout";
		final Long memberId = 1L;
		final Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.delete(url)
				.principal(auth)
		);

		// then
		resultActions.andExpect(status().isNoContent());
		verify(authService).logout(memberId);
	}

	@Test
	@DisplayName("Fails logout and returns unauthorized when principal is not a Long")
	void Given_InvalidPrincipalType_When_Logout_Then_ReturnsUnauthorized() throws Exception {
		// given
		final String url = "/v1/auth/logout";
		final String invalidPrincipal = "not-a-long";
		final Authentication auth = new UsernamePasswordAuthenticationToken(invalidPrincipal, null);
		final ServerErrorResult errorResult = ServerErrorResult.UNAUTHORIZED_ACCESS;

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.delete(url)
				.principal(auth)
		);

		// then
		resultActions.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorResult.name()))
			.andExpect(jsonPath("$.message").value(errorResult.getMessage()));
	}

}
