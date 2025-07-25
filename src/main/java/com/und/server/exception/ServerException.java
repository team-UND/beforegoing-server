package com.und.server.exception;

import lombok.Getter;

@Getter
public class ServerException extends RuntimeException {

	private final ServerErrorResult errorResult;

	public ServerException(ServerErrorResult errorResult) {
		super(errorResult.getMessage());
		this.errorResult = errorResult;
	}

	public ServerException(ServerErrorResult errorResult, Throwable cause) {
		super(errorResult.getMessage(), cause);
		this.errorResult = errorResult;
	}

}
