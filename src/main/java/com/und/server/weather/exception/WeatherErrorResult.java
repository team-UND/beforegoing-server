package com.und.server.weather.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherErrorResult implements ErrorResult {

	INVALID_COORDINATES(
		HttpStatus.BAD_REQUEST, "Invalid location coordinates"),
	DATE_OUT_OF_RANGE(
		HttpStatus.BAD_REQUEST, "Date is out of range (maximum +3 days)"),
	KMA_API_ERROR(
		HttpStatus.SERVICE_UNAVAILABLE, "Failed to call KMA weather API"),
	OPEN_METEO_API_ERROR(
		HttpStatus.SERVICE_UNAVAILABLE, "Failed to call Open-Meteo API"),
	WEATHER_SERVICE_ERROR(
		HttpStatus.SERVICE_UNAVAILABLE, "An error occurred while processing weather service");

	private final HttpStatus httpStatus;
	private final String message;

}
