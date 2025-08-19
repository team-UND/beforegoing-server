package com.und.server.weather.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 날씨 관련 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum WeatherErrorResult implements ErrorResult {

	// 위치 관련 에러
	INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "위치 좌표가 올바르지 않습니다"),
	GRID_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "격자 좌표 변환에 실패했습니다"),

	// 날짜 관련 에러
	INVALID_DATE(HttpStatus.BAD_REQUEST, "날짜가 올바르지 않습니다"),
	DATE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "조회 가능한 날짜 범위를 벗어났습니다 (최대 +3일)"),

	// API 호출 관련 에러
	KMA_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "기상청 API 호출에 실패했습니다"),
	OPEN_METEO_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Open-Meteo API 호출에 실패했습니다"),
	API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "API 호출 한도를 초과했습니다"),

	// 데이터 처리 관련 에러
	WEATHER_DATA_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "날씨 데이터 파싱에 실패했습니다"),
	NO_WEATHER_DATA_AVAILABLE(HttpStatus.NOT_FOUND, "해당 지역/시간의 날씨 정보를 찾을 수 없습니다"),

	// 일반 에러
	WEATHER_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "날씨 서비스 처리 중 오류가 발생했습니다");

	private final HttpStatus httpStatus;
	private final String message;
}
