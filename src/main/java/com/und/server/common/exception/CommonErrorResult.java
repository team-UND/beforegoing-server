package com.und.server.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorResult implements ErrorResult {

	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid Parameter"),
	UNKNOWN_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown Exception");

	private final HttpStatus httpStatus;
	private final String message;

}
