package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.OpenMeteoWeatherApiResultDto;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.dto.api.OpenMeteoResponse;
import com.und.server.weather.dto.api.OpenMeteoWeatherResponse;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.exception.KmaApiException;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.exception.WeatherException;
import com.und.server.weather.util.GridConverter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WeatherApiProcessor {

	private static final long API_TIMEOUT_SEC = 5;
	private final KmaApiService kmaApiService;
	private final OpenMeteoApiService openMeteoApiService;
	private final Executor weatherExecutor;

	public WeatherApiProcessor(
		KmaApiService kmaApiService,
		OpenMeteoApiService openMeteoApiService,
		@Qualifier("weatherExecutor") Executor weatherExecutor
	) {
		this.kmaApiService = kmaApiService;
		this.openMeteoApiService = openMeteoApiService;
		this.weatherExecutor = weatherExecutor;
	}


	public WeatherApiResultDto callTodayWeather(
		final WeatherRequest weatherRequest,
		final TimeSlot timeSlot,
		final LocalDate today
	) {
		final Double latitude = weatherRequest.latitude();
		final Double longitude = weatherRequest.longitude();
		final GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);

		CompletableFuture<KmaWeatherResponse> weatherFuture =
			CompletableFuture.supplyAsync(() -> kmaApiService.callWeatherApi(gridPoint, timeSlot, today),
					weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		CompletableFuture<OpenMeteoResponse> openMeteoFuture =
			CompletableFuture.supplyAsync(() -> openMeteoApiService.callDustUvApi(latitude, longitude, today),
					weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		try {
			KmaWeatherResponse weatherData = weatherFuture.join();
			OpenMeteoResponse dustUvData = openMeteoFuture.join();
			return WeatherApiResultDto.from(weatherData, dustUvData);

		} catch (CompletionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof WeatherException we) {
				throw we;
			}
			if (cause instanceof TimeoutException) {
				log.error("KMA API timeout during today slot data processing", cause);
				throw new KmaApiException(WeatherErrorResult.KMA_TIMEOUT, cause);
			}
			log.error("Unexpected error during today slot data processing", cause);
			throw new WeatherException(WeatherErrorResult.WEATHER_SERVICE_ERROR, cause);

		} catch (Exception e) {
			log.error("General error during today slot data processing", e);
			throw new WeatherException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}


	public WeatherApiResultDto callFutureWeather(
		final WeatherRequest weatherRequest,
		final TimeSlot timeSlot,
		final LocalDate today,
		final LocalDate targetDate
	) {
		final Double latitude = weatherRequest.latitude();
		final Double longitude = weatherRequest.longitude();
		final GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);

		CompletableFuture<KmaWeatherResponse> weatherFuture =
			CompletableFuture.supplyAsync(() -> kmaApiService.callWeatherApi(gridPoint, timeSlot, today),
					weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		CompletableFuture<OpenMeteoResponse> openMeteoFuture =
			CompletableFuture.supplyAsync(() -> openMeteoApiService.callDustUvApi(latitude, longitude, targetDate),
					weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		try {
			KmaWeatherResponse weatherData = weatherFuture.join();
			OpenMeteoResponse dustUvData = openMeteoFuture.join();
			return WeatherApiResultDto.from(weatherData, dustUvData);

		} catch (CompletionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof WeatherException we) {
				throw we;
			}
			if (cause instanceof TimeoutException) {
				log.error("KMA API timeout during future day data processing", cause);
				throw new KmaApiException(WeatherErrorResult.KMA_TIMEOUT, cause);
			}
			log.error("Unexpected error during future day data processing", cause);
			throw new WeatherException(WeatherErrorResult.WEATHER_SERVICE_ERROR, cause);

		} catch (Exception e) {
			log.error("General error during future day data processing", e);
			throw new WeatherException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}


	public OpenMeteoWeatherApiResultDto callOpenMeteoFallBackWeather(
		final WeatherRequest weatherRequest,
		final LocalDate targetDate
	) {
		final Double latitude = weatherRequest.latitude();
		final Double longitude = weatherRequest.longitude();

		CompletableFuture<OpenMeteoWeatherResponse> weatherFuture =
			CompletableFuture.supplyAsync(() -> openMeteoApiService.callWeatherApi(latitude, longitude, targetDate),
					weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		CompletableFuture<OpenMeteoResponse> openMeteoFuture =
			CompletableFuture.supplyAsync(() -> openMeteoApiService.callDustUvApi(latitude, longitude, targetDate),
					weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		try {
			OpenMeteoWeatherResponse weatherData = weatherFuture.join();
			OpenMeteoResponse dustUvData = openMeteoFuture.join();
			return OpenMeteoWeatherApiResultDto.from(weatherData, dustUvData);

		} catch (CompletionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof WeatherException we) {
				throw we;
			}
			log.error("Unexpected error during Open-Meteo KMA future day data processing", cause);
			throw new WeatherException(WeatherErrorResult.WEATHER_SERVICE_ERROR, cause);

		} catch (Exception e) {
			log.error("General error during Open-Meteo KMA future day data processing", e);
			throw new WeatherException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}

}
