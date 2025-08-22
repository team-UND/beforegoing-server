package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.dto.api.OpenMeteoResponse;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.exception.WeatherErrorResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherDecisionService {

	private final KmaWeatherExtractor kmaWeatherExtractor;
	private final FineDustExtractor fineDustExtractor;
	private final UvIndexExtractor uvIndexExtractor;
	private final FutureWeatherDecisionSelector futureWeatherDecisionSelector;


	public TimeSlotWeatherCacheData getTodayWeatherCacheData(
		WeatherApiResultDto weatherApiResult,
		TimeSlot currentSlot,
		LocalDate today
	) {
		KmaWeatherResponse kmaWeatherResponse = weatherApiResult.kmaWeatherResponse();
		OpenMeteoResponse openMeteoResponse = weatherApiResult.openMeteoResponse();

		List<Integer> slotHours = currentSlot.getForecastHours();

		Map<Integer, WeatherType> weathersByHour =
			kmaWeatherExtractor.extractWeatherForHours(kmaWeatherResponse, slotHours, today);
		Map<Integer, FineDustType> dustByHour =
			fineDustExtractor.extractDustForHours(openMeteoResponse, slotHours, today);
		Map<Integer, UvType> uvByHour =
			uvIndexExtractor.extractUvForHours(openMeteoResponse, slotHours, today);

		Map<String, WeatherCacheData> hourlyData = processHourlyData(
			weathersByHour, dustByHour, uvByHour, slotHours);


		for (Map.Entry<String, WeatherCacheData> entry : hourlyData.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}

		return TimeSlotWeatherCacheData.from(hourlyData);
	}


	public WeatherCacheData getFutureWeatherCacheData(WeatherApiResultDto weatherApiResult, LocalDate targetDate) {
		KmaWeatherResponse kmaWeatherResponse = weatherApiResult.kmaWeatherResponse();
		OpenMeteoResponse openMeteoResponse = weatherApiResult.openMeteoResponse();

		List<Integer> allHours = TimeSlot.getAllDayHours();

		Map<Integer, WeatherType> weatherMap =
			kmaWeatherExtractor.extractWeatherForHours(kmaWeatherResponse, allHours, targetDate);
		Map<Integer, FineDustType> dustMap =
			fineDustExtractor.extractDustForHours(openMeteoResponse, allHours, targetDate);
		Map<Integer, UvType> uvMap =
			uvIndexExtractor.extractUvForHours(openMeteoResponse, allHours, targetDate);

		System.out.println(weatherMap);
		System.out.println(dustMap);
		System.out.println(uvMap);

		WeatherType worstWeather =
			futureWeatherDecisionSelector.calculateWorstWeather(weatherMap.values().stream().toList());
		FineDustType worstFineDust =
			futureWeatherDecisionSelector.calculateWorstFineDust(dustMap.values().stream().toList());
		UvType worstUv =
			futureWeatherDecisionSelector.calculateWorstUv(uvMap.values().stream().toList());

		return WeatherCacheData.from(worstWeather, worstFineDust, worstUv);
	}


	private Map<String, WeatherCacheData> processHourlyData(
		Map<Integer, WeatherType> weathersByHour,
		Map<Integer, FineDustType> dustByHour,
		Map<Integer, UvType> uvByHour,
		List<Integer> targetHours
	) {
		Map<String, WeatherCacheData> hourlyData = new HashMap<>();

		try {
			for (int hour : targetHours) {
				WeatherType weather = weathersByHour.getOrDefault(hour, WeatherType.DEFAULT);
				FineDustType dust = dustByHour.getOrDefault(hour, FineDustType.DEFAULT);
				UvType uv = uvByHour.getOrDefault(hour, UvType.DEFAULT);

				WeatherCacheData weatherCacheData = WeatherCacheData.from(weather, dust, uv);

				hourlyData.put(String.format("%02d", hour), weatherCacheData);
			}
		} catch (Exception e) {
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
		return hourlyData;
	}

}
