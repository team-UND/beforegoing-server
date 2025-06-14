package com.und.server.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JwtAuthenticationEntryPointTest {

	private JwtAuthenticationEntryPoint entryPoint;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private AuthenticationException authException;

	private StringWriter responseWriter;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this);
		entryPoint = new JwtAuthenticationEntryPoint();
		responseWriter = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
	}

	@Test
	void setUnauthorizedStatusAndWriteJsonMessage() throws Exception {
		// given
		when(authException.getMessage()).thenReturn("Invalid token");

		// when
		entryPoint.commence(request, response, authException);

		// then
		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(response).setContentType("application/json;charset=UTF-8");

		final String expectedJson = "{ \"message\": \"Invalid token\" }";
		assertThat(responseWriter.toString()).isEqualTo(expectedJson);
	}

	@Test
	void ueDefaultMessageWhenExceptionMessageIsNull() throws Exception {
		// given
		when(authException.getMessage()).thenReturn(null);

		// when
		entryPoint.commence(request, response, authException);

		// then
		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(response).setContentType("application/json;charset=UTF-8");

		String expectedJson = "{ \"message\": \"Authentication Failed\" }";
		assertThat(responseWriter.toString()).isEqualTo(expectedJson);
	}
}
