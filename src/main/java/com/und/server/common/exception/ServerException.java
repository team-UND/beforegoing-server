package com.und.server.common.exception;

import lombok.Getter;

@Getter
public class ServerException extends RuntimeException {

	private final ServerErrorResult errorResult;

	public ServerException(final ServerErrorResult errorResult) {
		super(errorResult.getMessage());
		this.errorResult = errorResult;
	}

	public ServerException(final ServerErrorResult errorResult, final Throwable cause) {
		super(errorResult.getMessage(), cause);
		this.errorResult = errorResult;
	}

}
