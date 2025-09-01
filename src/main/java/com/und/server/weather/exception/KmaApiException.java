package com.und.server.weather.exception;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;

@Getter
public class KmaApiException extends WeatherException {

	public KmaApiException(ErrorResult errorResult) {
		super(errorResult);
	}

	public KmaApiException(ErrorResult errorResult, Throwable cause) {
		super(errorResult, cause);
	}

}
