package com.und.server.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.exception.ServerErrorResult;

class CustomAuthenticationEntryPointTest {

	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private record ErrorResponse(String code, Object message) { }

	@BeforeEach
	void init() {
		customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper);
	}

	@Test
	@DisplayName("Writes an unauthorized access error to the response when authentication fails")
	void Given_AuthenticationFailure_When_Commence_Then_WritesUnauthorizedErrorResponse() throws Exception {
		// given
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final AuthenticationException authException = mock(AuthenticationException.class);
		final ServerErrorResult expectedError = ServerErrorResult.UNAUTHORIZED_ACCESS;

		// when
		customAuthenticationEntryPoint.commence(request, response, authException);

		// then
		assertThat(response.getStatus()).isEqualTo(expectedError.getHttpStatus().value());
		assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");

		ErrorResponse actualResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
		assertThat(actualResponse.code()).isEqualTo(expectedError.name());
		assertThat(actualResponse.message()).isEqualTo(expectedError.getMessage());
	}

}
