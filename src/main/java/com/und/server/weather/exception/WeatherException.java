package com.und.server.weather.exception;

import com.und.server.common.exception.ErrorResult;
import com.und.server.common.exception.ServerException;

import lombok.Getter;

@Getter
public class WeatherException extends ServerException {

	public WeatherException(ErrorResult errorResult) {
		super(errorResult);
	}

	public WeatherException(ErrorResult errorResult, Throwable cause) {
		super(errorResult, cause);
	}

}
