package com.und.server.common.exception;

import lombok.Getter;

@Getter
public class ServerException extends RuntimeException {

	private final ErrorCode errorResult;

	public ServerException(final ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorResult = errorCode;
	}

	public ServerException(final ErrorCode errorCode, final Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorResult = errorCode;
	}

}
