package com.und.server.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.exception.ServerErrorResult;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

	private final ObjectMapper objectMapper;

	public void sendErrorResponse(HttpServletResponse response, ServerErrorResult errorResult) throws IOException {
		response.setStatus(errorResult.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(
			objectMapper.writeValueAsString(new ErrorResponse(errorResult.name(), errorResult.getMessage()))
		);
	}

	private record ErrorResponse(String code, Object message) { }
}
