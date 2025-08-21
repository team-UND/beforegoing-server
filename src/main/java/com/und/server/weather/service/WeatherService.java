package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
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
	private final WeatherCacheService cacheService;

	public WeatherResponse getWeatherInfo(WeatherRequest weatherRequest, LocalDate date) {
		LocalDateTime now = LocalDateTime.now();
		LocalDate today = now.toLocalDate();

		validateLocation(weatherRequest);
		validateDate(date, today);

		boolean isToday = date.equals(today);

		try {
			if (isToday) {
				TimeSlotWeatherCacheData todayWeatherCacheData =
					cacheService.getTodayWeather(weatherRequest.latitude(), weatherRequest.longitude(), now);
				return extractTodayDataByTime(todayWeatherCacheData, now.getHour());
			} else {
				WeatherCacheData futureWeatherCacheData =
					cacheService.getFutureWeather(weatherRequest.latitude(), weatherRequest.longitude(), now, date);
				return futureWeatherCacheData.toWeatherResponse();
			}
		} catch (Exception e) {
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}


	private WeatherResponse extractTodayDataByTime(TimeSlotWeatherCacheData data, int hour) {
		WeatherCacheData weatherCacheData = data.getHourlyData(hour);

		System.out.println("찾은캐시");
		System.out.println(weatherCacheData);
		if (weatherCacheData == null || !weatherCacheData.isValid()) {
			log.warn("유효하지 않은 시간별 데이터: hour={}", hour);
			return createDefaultResponse();
		}

		log.debug("오늘 데이터 추출 완료: {}시 - 날씨:{}, 미세먼지:{}, UV:{}",
			hour, weatherCacheData.getWeather(), weatherCacheData.getDust(), weatherCacheData.getUv());

		return weatherCacheData.toWeatherResponse();
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

	private WeatherResponse createDefaultResponse() {
		return WeatherResponse.from(
			WeatherType.DEFAULT,
			FineDustType.DEFAULT,
			UvType.DEFAULT
		);
	}

}
