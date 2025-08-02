package com.und.server.common.exception;

import java.util.List;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.und.server.common.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;

@Hidden // Exclude @RestControllerAdvice from Swagger
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private ResponseEntity<Object> buildErrorResponse(final ErrorResult errorResult, final Object message) {
		return ResponseEntity.status(errorResult.getHttpStatus())
			.body(new ErrorResponse(errorResult.name(), message));
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		final MethodArgumentNotValidException ex,
		final HttpHeaders headers,
		final HttpStatusCode status,
		final WebRequest request
	) {
		final List<String> errorList = ex.getBindingResult()
			.getAllErrors()
			.stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.toList();

		log.warn("Invalid DTO Parameter Errors: {}", errorList);
		return this.buildErrorResponse(ServerErrorResult.INVALID_PARAMETER, errorList);
	}

	@ExceptionHandler({ServerException.class})
	public ResponseEntity<Object> handleRestApiException(final ServerException exception) {
		final ErrorResult errorResult = exception.getErrorResult();
		log.warn("ServerException occur: ", exception);

		return this.buildErrorResponse(
			errorResult,
			errorResult.getMessage()
		);
	}

	@ExceptionHandler({Exception.class})
	public ResponseEntity<Object> handleException(final Exception exception) {
		log.warn("Exception occur: ", exception);

		return this.buildErrorResponse(
			ServerErrorResult.UNKNOWN_EXCEPTION,
			ServerErrorResult.UNKNOWN_EXCEPTION.getMessage()
		);
	}

}
