package com.und.server.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.dto.ErrorResponse;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.jwt.JwtProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private FilterChain filterChain;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final SecurityErrorResponseWriter securityErrorResponseWriter = new SecurityErrorResponseWriter(
		objectMapper
	);

	@BeforeEach
	void init() {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, securityErrorResponseWriter);
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Sets an error response and stops the filter chain for an expired token on a protected path")
	void Given_ExpiredTokenOnProtectedRoute_When_Filter_Then_ErrorResponseIsSetAndChainStops()
		throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final String token = "expired.token.string";
		final String protectedPath = "/v1/protected/resource";
		request.setRequestURI(protectedPath);
		request.setServletPath(protectedPath);
		request.addHeader("Authorization", "Bearer " + token);

		final ServerErrorResult expectedError = ServerErrorResult.EXPIRED_TOKEN;
		when(jwtProvider.getAuthentication(token)).thenThrow(new ServerException(expectedError));

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(expectedError.getHttpStatus().value());
		assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");

		final ErrorResponse actualResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
		assertThat(actualResponse.code()).isEqualTo(expectedError.name());
		assertThat(actualResponse.message()).isEqualTo(expectedError.getMessage());

		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	@DisplayName("Sets an error response and stops the filter chain for a token with an invalid signature")
	void Given_TokenWithInvalidSignature_When_Filter_Then_ErrorResponseIsSetAndChainStops()
		throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final String token = "invalid.signature.token.string";
		final String path = "/v1/protected/resource";
		request.setRequestURI(path);
		request.setServletPath(path);
		request.addHeader("Authorization", "Bearer " + token);

		final ServerErrorResult expectedError = ServerErrorResult.INVALID_TOKEN_SIGNATURE;
		when(jwtProvider.getAuthentication(token)).thenThrow(new ServerException(expectedError));

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(expectedError.getHttpStatus().value());
		assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");

		final ErrorResponse actualResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
		assertThat(actualResponse.code()).isEqualTo(expectedError.name());
		assertThat(actualResponse.message()).isEqualTo(expectedError.getMessage());

		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	@DisplayName("Sets an error response for an expired token on a now-restricted auth path")
	void Given_ExpiredTokenOnLoginPath_When_Filter_Then_ErrorResponseIsSetAndChainStops()
		throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final String token = "expired.token.string";
		final String loginPath = "/v1/auth/login";
		request.setRequestURI(loginPath);
		request.setServletPath(loginPath);
		request.addHeader("Authorization", "Bearer " + token);

		final ServerErrorResult expectedError = ServerErrorResult.EXPIRED_TOKEN;
		when(jwtProvider.getAuthentication(token)).thenThrow(new ServerException(expectedError));

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(expectedError.getHttpStatus().value());
		final ErrorResponse actualResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
		assertThat(actualResponse.code()).isEqualTo(expectedError.name());
		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	@DisplayName("Continues the filter chain for an expired token on the token reissue path")
	void Given_ExpiredTokenOnTokenReissuePath_When_Filter_Then_ChainContinues() throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final String token = "expired.token.string";
		final String permissivePath = "/v1/auth/tokens"; // The specific permissive path
		request.setRequestURI(permissivePath);
		request.setServletPath(permissivePath);
		request.addHeader("Authorization", "Bearer " + token);

		final ServerErrorResult expectedError = ServerErrorResult.EXPIRED_TOKEN;
		when(jwtProvider.getAuthentication(token)).thenThrow(new ServerException(expectedError));

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(200); // No error status is set
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Continues filter chain without setting authentication when token is not present")
	void Given_NoToken_When_Filter_Then_ContextIsEmptyAndChainContinues() throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(jwtProvider, never()).getAuthentication(anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Continues filter chain without setting authentication for an invalid token format")
	void Given_InvalidTokenFormat_When_Filter_Then_ContextIsEmptyAndChainContinues()
		throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final String invalidFormatToken = "invalid.token.string";
		request.addHeader("Authorization", invalidFormatToken);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(jwtProvider, never()).getAuthentication(anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Sets authentication in SecurityContext for a valid token")
	void Given_ValidToken_When_Filter_Then_AuthenticationIsSetInContext() throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final String token = "valid.token.string";
		request.addHeader("Authorization", "Bearer " + token);

		final Authentication authentication = mock(Authentication.class);
		when(jwtProvider.getAuthentication(token)).thenReturn(authentication);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
		verify(filterChain).doFilter(request, response);
	}

}
