package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.OpenMeteoWeatherApiResultDto;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.infrastructure.dto.KmaWeatherResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherDecisionService {

	private final KmaWeatherExtractor kmaWeatherExtractor;
	private final OpenMeteoWeatherExtractor openMeteoWeatherExtractor;
	private final FineDustExtractor fineDustExtractor;
	private final UvIndexExtractor uvIndexExtractor;
	private final FutureWeatherDecisionSelector futureWeatherDecisionSelector;


	public Map<String, WeatherCacheData> getTodayWeatherCacheData(
		final WeatherApiResultDto weatherApiResult,
		final TimeSlot currentSlot,
		final LocalDate today
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

		return processHourlyData(weathersByHour, dustByHour, uvByHour, slotHours);
	}


	public WeatherCacheData getFutureWeatherCacheData(
		final WeatherApiResultDto weatherApiResult, final LocalDate targetDate
	) {
		KmaWeatherResponse kmaWeatherResponse = weatherApiResult.kmaWeatherResponse();
		OpenMeteoResponse openMeteoResponse = weatherApiResult.openMeteoResponse();

		List<Integer> allHours = TimeSlot.getAllDayHours();

		Map<Integer, WeatherType> weatherMap =
			kmaWeatherExtractor.extractWeatherForHours(kmaWeatherResponse, allHours, targetDate);
		Map<Integer, FineDustType> dustMap =
			fineDustExtractor.extractDustForHours(openMeteoResponse, allHours, targetDate);
		Map<Integer, UvType> uvMap =
			uvIndexExtractor.extractUvForHours(openMeteoResponse, allHours, targetDate);

		WeatherType worstWeather =
			futureWeatherDecisionSelector.calculateWorstWeather(weatherMap.values().stream().toList());
		FineDustType worstFineDust =
			futureWeatherDecisionSelector.calculateWorstFineDust(dustMap.values().stream().toList());
		UvType worstUv =
			futureWeatherDecisionSelector.calculateWorstUv(uvMap.values().stream().toList());

		return WeatherCacheData.from(worstWeather, worstFineDust, worstUv);
	}


	public Map<String, WeatherCacheData> getTodayWeatherCacheDataFallback(
		final OpenMeteoWeatherApiResultDto weatherApiResult,
		final TimeSlot currentSlot,
		final LocalDate today
	) {
		OpenMeteoWeatherResponse openMeteoWeatherResponse = weatherApiResult.openMeteoWeatherResponse();
		OpenMeteoResponse openMeteoResponse = weatherApiResult.openMeteoResponse();

		List<Integer> slotHours = currentSlot.getForecastHours();

		Map<Integer, WeatherType> weathersByHour =
			openMeteoWeatherExtractor.extractWeatherForHours(openMeteoWeatherResponse, slotHours, today);
		Map<Integer, FineDustType> dustByHour =
			fineDustExtractor.extractDustForHours(openMeteoResponse, slotHours, today);
		Map<Integer, UvType> uvByHour =
			uvIndexExtractor.extractUvForHours(openMeteoResponse, slotHours, today);

		return processHourlyData(weathersByHour, dustByHour, uvByHour, slotHours);
	}


	public WeatherCacheData getFutureWeatherCacheDataFallback(
		final OpenMeteoWeatherApiResultDto weatherApiResult,
		final LocalDate targetDate
	) {
		OpenMeteoWeatherResponse openMeteoWeatherResponse = weatherApiResult.openMeteoWeatherResponse();
		OpenMeteoResponse openMeteoResponse = weatherApiResult.openMeteoResponse();

		List<Integer> allHours = TimeSlot.getAllDayHours();

		Map<Integer, WeatherType> weatherMap =
			openMeteoWeatherExtractor.extractWeatherForHours(openMeteoWeatherResponse, allHours, targetDate);
		Map<Integer, FineDustType> dustMap =
			fineDustExtractor.extractDustForHours(openMeteoResponse, allHours, targetDate);
		Map<Integer, UvType> uvMap =
			uvIndexExtractor.extractUvForHours(openMeteoResponse, allHours, targetDate);

		WeatherType worstWeather =
			futureWeatherDecisionSelector.calculateWorstWeather(weatherMap.values().stream().toList());
		FineDustType worstFineDust =
			futureWeatherDecisionSelector.calculateWorstFineDust(dustMap.values().stream().toList());
		UvType worstUv =
			futureWeatherDecisionSelector.calculateWorstUv(uvMap.values().stream().toList());

		return WeatherCacheData.from(worstWeather, worstFineDust, worstUv);
	}


	private Map<String, WeatherCacheData> processHourlyData(
		final Map<Integer, WeatherType> weathersByHour,
		final Map<Integer, FineDustType> dustByHour,
		final Map<Integer, UvType> uvByHour,
		final List<Integer> targetHours
	) {
		Map<String, WeatherCacheData> hourlyData = new HashMap<>();

		for (int hour : targetHours) {
			WeatherType weather = weathersByHour.getOrDefault(hour, WeatherType.DEFAULT);
			FineDustType dust = dustByHour.getOrDefault(hour, FineDustType.DEFAULT);
			UvType uv = uvByHour.getOrDefault(hour, UvType.DEFAULT);

			WeatherCacheData weatherCacheData = WeatherCacheData.from(weather, dust, uv);

			hourlyData.put(String.format("%02d", hour), weatherCacheData);
		}

		return hourlyData;
	}

}
