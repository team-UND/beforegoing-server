package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;
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
	private final WeatherCacheService weatherCacheService;


	public WeatherResponse getWeatherInfo(
		final WeatherRequest weatherRequest, final LocalDate date
	) {
		LocalDateTime nowDateTime = LocalDateTime.now();
		LocalDate today = nowDateTime.toLocalDate();

		validateLocation(weatherRequest);
		validateDate(date, today);

		boolean isToday = date.equals(today);
		try {
			if (isToday) {
				return getTodayWeather(weatherRequest, nowDateTime);
			} else {
				return getFutureWeather(weatherRequest, nowDateTime, date);
			}
		} catch (Exception e) {
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}


	private WeatherResponse getTodayWeather(
		final WeatherRequest weatherRequest, final LocalDateTime nowDateTime
	) {
		TimeSlotWeatherCacheData todayWeatherCacheData =
			weatherCacheService.getTodayWeatherCache(weatherRequest, nowDateTime);

		WeatherCacheData weatherCacheData = todayWeatherCacheData.getHourlyData(nowDateTime.getHour());

		if (weatherCacheData == null || !weatherCacheData.isValid()) {
			return WeatherCacheData.getDefault().toWeatherResponse();
		}

		return weatherCacheData.toWeatherResponse();
	}

	private WeatherResponse getFutureWeather(
		final WeatherRequest weatherRequest,
		final LocalDateTime nowDateTime, final LocalDate targetDate
	) {
		WeatherCacheData futureWeatherCacheData =
			weatherCacheService.getFutureWeatherCache(weatherRequest, nowDateTime, targetDate);

		if (futureWeatherCacheData == null || !futureWeatherCacheData.isValid()) {
			return WeatherCacheData.getDefault().toWeatherResponse();
		}

		return futureWeatherCacheData.toWeatherResponse();
	}

	private void validateLocation(final WeatherRequest request) {
		if (request.latitude() < -90
			|| request.latitude() > 90
			|| request.longitude() < -180
			|| request.longitude() > 180) {
			throw new ServerException(WeatherErrorResult.INVALID_COORDINATES);
		}
	}

	private void validateDate(final LocalDate requestDate, final LocalDate today) {
		LocalDate maxDate = today.plusDays(MAX_FUTURE_DATE);

		if (requestDate.isBefore(today) || requestDate.isAfter(maxDate)) {
			throw new ServerException(WeatherErrorResult.DATE_OUT_OF_RANGE);
		}
	}

}
