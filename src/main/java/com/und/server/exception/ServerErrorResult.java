package com.und.server.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServerErrorResult {

	INVALID_NONCE(HttpStatus.BAD_REQUEST, "Invalid nonce"),
	INVALID_TOKEN_PAYLOAD(HttpStatus.BAD_REQUEST, "Invalid Token Payload"),
	INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "Invalid Provider"),
	PUBLIC_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, "Public Key Not Found"),
	INVALID_PUBLIC_KEY(HttpStatus.BAD_REQUEST, "Invalid Public Key"),
	PUBLIC_KEY_INVALID(HttpStatus.BAD_REQUEST, "Public Key Invalid"),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Expired Token"),
	MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "Malformed Token"),
	INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "Invalid Signature"),
	UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "Unsupported Token"),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "Invalid Token"),
	MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Member Not Found"),
	UNKNOWN_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown Exception");

	private final HttpStatus httpStatus;
	private final String message;

}
