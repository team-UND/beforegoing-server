package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.exception.WeatherErrorResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 정보 조회 서비스 (Redis 캐싱 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

	private final WeatherCacheService cacheService;

	/**
	 * 날씨 정보 조회 (오늘/미래 자동 분기 처리 - Redis 캐싱 적용)
	 */
	public WeatherResponse getWeatherInfo(WeatherRequest request) {
		log.info("날씨 정보 조회 시작: lat={}, lng={}, date={}",
			request.latitude(), request.longitude(), request.date());

		// 입력값 검증
		validateRequest(request);

		LocalDate today = LocalDate.now();
		boolean isToday = request.date().equals(today);

		try {
			if (isToday) {
				// 오늘 날씨: 현재 시간 기준으로 해당 시간대 데이터 반환
				LocalDateTime now = LocalDateTime.now();
				return cacheService.getTodayWeather(request.latitude(), request.longitude(), now);
			} else {
				// 미래 날씨: 하루 전체의 최악값(날씨) + 평균값(미세먼지, UV) 반환
				return cacheService.getFutureWeather(request.latitude(), request.longitude(), request.date());
			}
		} catch (Exception e) {
			log.error("날씨 정보 조회 중 예상치 못한 오류 발생", e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}

	/**
	 * 요청 데이터 검증
	 */
	private void validateRequest(WeatherRequest request) {
		// 위도 경도 범위 검증 (수정: 올바른 범위로 변경)
		if (request.latitude() < -90 || request.latitude() > 90 ||
			request.longitude() < -180 || request.longitude() > 180) {
			throw new ServerException(WeatherErrorResult.INVALID_COORDINATES);
		}

		// 날짜 범위 검증 (오늘부터 +3일까지)
		LocalDate today = LocalDate.now();
		LocalDate maxDate = today.plusDays(3);

		if (request.date().isBefore(today) || request.date().isAfter(maxDate)) {
			throw new ServerException(WeatherErrorResult.DATE_OUT_OF_RANGE);
		}
	}

}
