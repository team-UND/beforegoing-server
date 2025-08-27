package com.und.server.terms.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsErrorResult implements ErrorResult {

	TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "Terms Not Found"),
	TERMS_ALREADY_EXISTS(HttpStatus.CONFLICT, "Terms Already Exists");

	private final HttpStatus httpStatus;
	private final String message;

}
