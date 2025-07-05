package com.und.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.dto.AuthRequest;
import com.und.server.dto.AuthResponse;
import com.und.server.dto.HandshakeRequest;
import com.und.server.dto.HandshakeResponse;
import com.und.server.dto.RefreshTokenRequest;
import com.und.server.exception.GlobalExceptionHandler;
import com.und.server.oauth.Provider;
import com.und.server.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

	@InjectMocks
	private AuthController authController;

	@Mock
	private AuthService authService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(authController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("Handshake successfully when request is valid")
	void handshakeSuccessfullyWhenRequestIsValid() throws Exception {
		// given
		final String url = "/api/v1/auth/nonce";
		final HandshakeRequest request = new HandshakeRequest(Provider.KAKAO);
		final HandshakeResponse response = new HandshakeResponse("generated-nonce");

		doReturn(response).when(authService).handshake(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		final HandshakeResponse result = objectMapper.readValue(
			resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
			HandshakeResponse.class
		);

		resultActions.andExpect(status().isOk());
		assertThat(result.nonce()).isEqualTo("generated-nonce");
	}

	@Test
	@DisplayName("Fail to handshake when provider is null")
	void failToHandshakeWhenProviderIsNull() throws Exception {
		// given
		final String url = "/api/v1/auth/nonce";
		final String invalidRequest = "{ \"provider\": null }";

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(invalidRequest)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	@ParameterizedTest
	@MethodSource("invalidLoginParameter")
	@DisplayName("Fail to login when request parameters are invalid")
	void failToLoginWhenRequestParametersAreInvalid(Provider provider, String idToken) throws Exception {
		// given
		final String url = "/api/v1/auth/login";

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(authRequest(provider, idToken)))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	private static Stream<Arguments> invalidLoginParameter() {
		return Stream.of(
			Arguments.of(null, "dummy.id.token"),
			Arguments.of(Provider.KAKAO, null)
		);
	}

	@Test
	@DisplayName("Login successfully when request is valid")
	void loginSuccessfullyWhenRequestIsValid() throws Exception {
		// given
		final String url = "/api/v1/auth/login";
		final AuthRequest authRequest = authRequest(Provider.KAKAO, "dummy.id.token");
		final AuthResponse authResponse = authResponse(
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
				resultActions.andReturn()
						.getResponse()
						.getContentAsString(StandardCharsets.UTF_8), AuthResponse.class);

		resultActions.andExpect(status().isOk());
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.accessToken()).isEqualTo("dummy.access.token");
		assertThat(response.accessTokenExpiresIn()).isEqualTo(10000);
		assertThat(response.refreshToken()).isEqualTo("dummy.refresh.token");
		assertThat(response.refreshTokenExpiresIn()).isEqualTo(20000);
	}

	private AuthRequest authRequest(final Provider provider, final String idToken) {
		return new AuthRequest(provider, idToken);
	}

	private AuthResponse authResponse(
		final String tokenType,
		final String accessToken,
		final Integer accessTokenExpiresIn,
		final String refreshToken,
		final Integer refreshTokenExpiresIn
	) {
		return new AuthResponse(
			tokenType,
			accessToken,
			accessTokenExpiresIn,
			refreshToken,
			refreshTokenExpiresIn
		);
	}

	@Test
	@DisplayName("Refresh token successfully when request is valid")
	void refreshTokenSuccessfullyWhenRequestIsValid() throws Exception {
		// given
		final String url = "/api/v1/auth/tokens";
		final RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(
			"dummy.access.token", "dummy.refresh.token"
		);
		final AuthResponse authResponse = authResponse(
			"Bearer",
			"new.access.token",
			3600,
			"dummy.refresh.token",
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
			resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
			AuthResponse.class
		);

		resultActions.andExpect(status().isOk());
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.accessToken()).isEqualTo("new.access.token");
		assertThat(response.refreshToken()).isEqualTo("dummy.refresh.token");
	}


	@Test
	@DisplayName("Fail to refresh token when refresh_token is null")
	void failToRefreshTokenWhenRefreshTokenIsNull() throws Exception {
		// given
		final String url = "/api/v1/auth/tokens";
		final RefreshTokenRequest invalidRequest = new RefreshTokenRequest("dummy.access.token", null);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(invalidRequest))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

}
