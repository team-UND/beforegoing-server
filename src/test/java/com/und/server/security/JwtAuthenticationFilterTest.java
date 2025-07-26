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

	private SecurityErrorResponseWriter securityErrorResponseWriter;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private record ErrorResponse(String code, Object message) {
	}

	@BeforeEach
	void init() {
		securityErrorResponseWriter = new SecurityErrorResponseWriter(objectMapper);
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, securityErrorResponseWriter);
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Sets an error response and stops the filter chain when token is invalid")
	void Given_InvalidToken_When_Filter_Then_ErrorResponseIsSetAndChainStops() throws ServletException, IOException {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final String token = "expired.token.string";
		request.addHeader("Authorization", "Bearer " + token);

		final ServerErrorResult expectedError = ServerErrorResult.EXPIRED_TOKEN;
		when(jwtProvider.getAuthentication(token)).thenThrow(new ServerException(expectedError));

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(expectedError.getHttpStatus().value());
		assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");

		ErrorResponse actualResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
		assertThat(actualResponse.code()).isEqualTo(expectedError.name());
		assertThat(actualResponse.message()).isEqualTo(expectedError.getMessage());

		verify(filterChain, never()).doFilter(request, response);
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
