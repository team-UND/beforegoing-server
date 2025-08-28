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
	WEATHER_SERVICE_ERROR(
		HttpStatus.SERVICE_UNAVAILABLE, "An error occurred while processing weather service"),
	WEATHER_SERVICE_TIMEOUT(
		HttpStatus.GATEWAY_TIMEOUT, "Weather service request timed out"),

	KMA_API_ERROR(
		HttpStatus.SERVICE_UNAVAILABLE, "Failed to call KMA weather API"),
	KMA_TIMEOUT(
		HttpStatus.GATEWAY_TIMEOUT, "KMA API request timed out"),
	KMA_BAD_REQUEST(
		HttpStatus.BAD_REQUEST, "Invalid request to KMA weather API"),
	KMA_SERVER_ERROR(
		HttpStatus.BAD_GATEWAY, "KMA API server error"),
	KMA_RATE_LIMIT(
		HttpStatus.TOO_MANY_REQUESTS, "KMA API rate limit exceeded"),
	KMA_PARSE_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse KMA API response"),

	OPEN_METEO_API_ERROR(
		HttpStatus.SERVICE_UNAVAILABLE, "Failed to call Open-Meteo API"),
	OPEN_METEO_TIMEOUT(
		HttpStatus.GATEWAY_TIMEOUT, "Open-Meteo API request timed out"),
	OPEN_METEO_BAD_REQUEST(
		HttpStatus.BAD_REQUEST, "Invalid request to Open-Meteo API"),
	OPEN_METEO_SERVER_ERROR(
		HttpStatus.BAD_GATEWAY, "Open-Meteo API server error"),
	OPEN_METEO_RATE_LIMIT(
		HttpStatus.TOO_MANY_REQUESTS, "Open-Meteo API rate limit exceeded"),
	OPEN_METEO_PARSE_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse Open-Meteo API response");

	private final HttpStatus httpStatus;
	private final String message;

}
