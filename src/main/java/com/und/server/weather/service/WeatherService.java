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

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

	private static final int MAX_FUTURE_DATE = 3;
	private final WeatherCacheService cacheService;

	public WeatherResponse getWeatherInfo(WeatherRequest weatherRequest, LocalDate date) {
		LocalDateTime now = LocalDateTime.now();
		LocalDate today = now.toLocalDate();

		validateLocation(weatherRequest);
		validateDate(date, today);

		boolean isToday = date.equals(today);

		try {
			if (isToday) {
				return cacheService.getTodayWeather(weatherRequest.latitude(), weatherRequest.longitude(), now);
			} else {
				return cacheService.getFutureWeather(weatherRequest.latitude(), weatherRequest.longitude(), date);
			}
		} catch (Exception e) {
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}


	private void validateLocation(WeatherRequest request) {
		if (request.latitude() < -90 || request.latitude() > 90 ||
			request.longitude() < -180 || request.longitude() > 180) {
			throw new ServerException(WeatherErrorResult.INVALID_COORDINATES);
		}
	}

	private void validateDate(LocalDate requestDate, LocalDate today) {
		LocalDate maxDate = today.plusDays(MAX_FUTURE_DATE);

		if (requestDate.isBefore(today) || requestDate.isAfter(maxDate)) {
			throw new ServerException(WeatherErrorResult.DATE_OUT_OF_RANGE);
		}
	}

}
