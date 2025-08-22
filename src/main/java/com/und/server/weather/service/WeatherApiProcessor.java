package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.weather.client.KmaWeatherClient;
import com.und.server.weather.client.OpenMeteoClient;
import com.und.server.weather.config.WeatherProperties;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.dto.api.OpenMeteoResponse;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.util.GridConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherApiProcessor {

	private final KmaWeatherClient kmaWeatherClient;
	private final OpenMeteoClient openMeteoClient;
	private final WeatherProperties weatherProperties;
	private final KmaWeatherExtractor kmaWeatherExtractor;
	private final FineDustExtractor fineDustExtractor;
	private final UvIndexExtractor uvIndexExtractor;
	private final FutureWeatherDecisionSelector futureWeatherDecisionSelector;


	public TimeSlotWeatherCacheData fetchTodaySlotData(
		WeatherRequest weatherRequest,
		TimeSlot currentSlot,
		LocalDate today
	) {
		Double latitude = weatherRequest.latitude();
		Double longitude = weatherRequest.longitude();
		GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);

		try {
			CompletableFuture<KmaWeatherResponse> weatherFuture =
				CompletableFuture.supplyAsync(() -> callKmaWeatherApi(gridPoint, currentSlot, today));
			CompletableFuture<OpenMeteoResponse> openMeteoFuture =
				CompletableFuture.supplyAsync(() -> callOpenMeteoApi(latitude, longitude, today));

			KmaWeatherResponse weatherData = weatherFuture.get();
			OpenMeteoResponse dustUvData = openMeteoFuture.get();

			List<Integer> slotHours = currentSlot.getForecastHours();

			Map<Integer, WeatherType> weathersByHour =
				kmaWeatherExtractor.extractWeatherForHours(weatherData, slotHours, today);
			Map<Integer, FineDustType> dustByHour =
				fineDustExtractor.extractDustForHours(dustUvData, slotHours, today);
			Map<Integer, UvType> uvByHour =
				uvIndexExtractor.extractUvForHours(dustUvData, slotHours, today);

			Map<String, WeatherCacheData> hourlyData = processHourlyData(
				weathersByHour, dustByHour, uvByHour, slotHours);


			for (Map.Entry<String, WeatherCacheData> entry : hourlyData.entrySet()) {
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}


			return TimeSlotWeatherCacheData.builder()
				.hours(hourlyData)
				.build();

		} catch (Exception e) {
			log.error("오늘 슬롯 데이터 처리 중 오류 발생", e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}


	public WeatherCacheData fetchFutureDayData(
		WeatherRequest weatherRequest,
		TimeSlot timeSlot,
		LocalDate today, LocalDate targetDate
	) {
		Double latitude = weatherRequest.latitude();
		Double longitude = weatherRequest.longitude();
		GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);

		try {
			CompletableFuture<KmaWeatherResponse> weatherFuture =
				CompletableFuture.supplyAsync(() -> callKmaWeatherApi(gridPoint, timeSlot, today));
			CompletableFuture<OpenMeteoResponse> openMeteoFuture =
				CompletableFuture.supplyAsync(() -> callOpenMeteoApi(latitude, longitude, targetDate));

			KmaWeatherResponse weatherData = weatherFuture.get();
			OpenMeteoResponse dustUvData = openMeteoFuture.get();

			List<Integer> allHours = TimeSlot.getAllDayHours();

			Map<Integer, WeatherType> weatherMap =
				kmaWeatherExtractor.extractWeatherForHours(weatherData, allHours, targetDate);
			Map<Integer, FineDustType> dustMap =
				fineDustExtractor.extractDustForHours(dustUvData, allHours, targetDate);
			Map<Integer, UvType> uvMap =
				uvIndexExtractor.extractUvForHours(dustUvData, allHours, targetDate);

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

		} catch (Exception e) {
			log.error("미래 하루 전체 데이터 처리 중 오류 발생", e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
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

	private KmaWeatherResponse callKmaWeatherApi(GridPoint gridPoint, TimeSlot timeSlot, LocalDate date) {
		try {
			String baseDate = timeSlot.getBaseDate(date).format(WeatherType.KMA_DATE_FORMATTER);
			String baseTime = timeSlot.getBaseTime();

			return kmaWeatherClient.getVilageForecast(
				weatherProperties.kma().serviceKey(),
				1,
				1000,
				"JSON",
				baseDate,
				baseTime,
				gridPoint.gridX(),
				gridPoint.gridY()
			);
		} catch (Exception e) {
			log.error("기상청 API 호출 실패", e);
			throw new ServerException(WeatherErrorResult.KMA_API_ERROR, e);
		}
	}

	private OpenMeteoResponse callOpenMeteoApi(Double latitude, Double longitude, LocalDate date) {
		String variables = String.join(",",
			FineDustType.OPEN_METEO_VARIABLES,
			UvType.OPEN_METEO_VARIABLES
		);
		try {
			return openMeteoClient.getForecast(
				latitude,
				longitude,
				variables,
				date.toString(),
				date.toString(),
				"Asia/Seoul"
			);
		} catch (Exception e) {
			log.error("Open-Meteo API 호출 실패", e);
			throw new ServerException(WeatherErrorResult.OPEN_METEO_API_ERROR, e);
		}
	}

}
