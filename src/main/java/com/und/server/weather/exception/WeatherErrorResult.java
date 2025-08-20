package com.und.server.weather.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Weather related error codes
 */
@Getter
@RequiredArgsConstructor
public enum WeatherErrorResult implements ErrorResult {

	// Location related errors
	INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "Invalid location coordinates"),
	GRID_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to convert grid coordinates"),

	// Date related errors
	INVALID_DATE(HttpStatus.BAD_REQUEST, "Invalid date"),
	DATE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "Date is out of range (maximum +3 days)"),

	// API call related errors
	KMA_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Failed to call KMA weather API"),
	OPEN_METEO_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Failed to call Open-Meteo API"),
	API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "API rate limit exceeded"),

	// Data processing related errors
	WEATHER_DATA_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse weather data"),
	NO_WEATHER_DATA_AVAILABLE(HttpStatus.NOT_FOUND, "No weather data available for the specified location/time"),

	// General errors
	WEATHER_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing weather service");

	private final HttpStatus httpStatus;
	private final String message;
}
