package com.und.server.common.exception;

import lombok.Getter;

@Getter
public class ServerException extends RuntimeException {

	private final ErrorResult errorResult;

	public ServerException(final ErrorResult errorResult) {
		super(errorResult.getMessage());
		this.errorResult = errorResult;
	}

	public ServerException(final ErrorResult errorResult, final Throwable cause) {
		super(errorResult.getMessage(), cause);
		this.errorResult = errorResult;
	}

}
