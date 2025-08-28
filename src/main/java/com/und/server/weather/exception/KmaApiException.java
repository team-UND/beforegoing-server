package com.und.server.weather.exception;

import lombok.Getter;

import com.und.server.common.exception.ErrorResult;
import com.und.server.common.exception.ServerException;

@Getter
public class KmaApiException extends WeatherException {

	public KmaApiException(ErrorResult errorResult) {
		super(errorResult);
	}

	public KmaApiException(ErrorResult errorResult, Throwable cause) {
		super(errorResult, cause);
	}

}
