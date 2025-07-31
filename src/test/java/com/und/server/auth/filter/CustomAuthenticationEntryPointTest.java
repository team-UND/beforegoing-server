package com.und.server.auth.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.common.dto.ErrorResponse;
import com.und.server.common.exception.ServerErrorResult;

class CustomAuthenticationEntryPointTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final SecurityErrorResponseWriter securityErrorResponseWriter = new SecurityErrorResponseWriter(
		objectMapper
	);
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint(
		securityErrorResponseWriter
	);

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

		final ErrorResponse actualResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
		assertThat(actualResponse.code()).isEqualTo(expectedError.name());
		assertThat(actualResponse.message()).isEqualTo(expectedError.getMessage());
	}

}
