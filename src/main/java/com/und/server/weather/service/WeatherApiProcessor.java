package com.und.server.weather.service;

import java.time.LocalDate;
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
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.dto.api.OpenMeteoResponse;
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


	public WeatherApiResultDto callTodayWeather(
		final WeatherRequest weatherRequest,
		final TimeSlot currentSlot,
		final LocalDate today
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

			return WeatherApiResultDto.from(weatherData, dustUvData);

		} catch (Exception e) {
			log.error("오늘 슬롯 데이터 처리 중 오류 발생", e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}

	}


	public WeatherApiResultDto callFutureWeather(
		final WeatherRequest weatherRequest,
		final TimeSlot timeSlot,
		final LocalDate today, final LocalDate targetDate
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

			return WeatherApiResultDto.from(weatherData, dustUvData);

		} catch (Exception e) {
			log.error("미래 하루 전체 데이터 처리 중 오류 발생", e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}


	private KmaWeatherResponse callKmaWeatherApi(
		final GridPoint gridPoint,
		final TimeSlot timeSlot,
		final LocalDate date
	) {
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

	private OpenMeteoResponse callOpenMeteoApi(
		final Double latitude, final Double longitude,
		final LocalDate date
	) {
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
