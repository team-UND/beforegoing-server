package com.und.server.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServerErrorResult {

	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid Parameter"),
	INVALID_NONCE(HttpStatus.BAD_REQUEST, "Invalid nonce"),
	INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "Invalid Provider"),
	PUBLIC_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, "Public Key Not Found"),
	INVALID_PUBLIC_KEY(HttpStatus.BAD_REQUEST, "Invalid Public Key"),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Expired Token"),
	MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "Malformed Token"),
	INVALID_TOKEN_SIGNATURE(HttpStatus.UNAUTHORIZED, "Invalid Token Signature"),
	UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "Unsupported Token"),
	WEAK_TOKEN_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "Token Key is Weak"),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "Invalid Token"),
	UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "Unauthorized Access"),
	// FIXME: Remove MEMBER_NOT_FOUND when deleting TestController
	MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Member Not Found"),
	UNKNOWN_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown Exception");

	private final HttpStatus httpStatus;
	private final String message;

}
