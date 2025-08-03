package com.und.server.auth.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorResult implements ErrorResult {

	INVALID_NONCE(HttpStatus.BAD_REQUEST, "Invalid nonce"),
	INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "Invalid Provider"),
	INVALID_PROVIDER_ID(HttpStatus.BAD_REQUEST, "Invalid Provider ID"),
	PUBLIC_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, "Public Key Not Found"),
	INVALID_PUBLIC_KEY(HttpStatus.BAD_REQUEST, "Invalid Public Key"),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Expired Token"),
	NOT_EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "Not Expired Token"),
	MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "Malformed Token"),
	INVALID_TOKEN_SIGNATURE(HttpStatus.UNAUTHORIZED, "Invalid Token Signature"),
	UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "Unsupported Token"),
	WEAK_TOKEN_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "Token Key is Weak"),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "Invalid Token"),
	UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "Unauthorized Access");

	private final HttpStatus httpStatus;
	private final String message;

}
