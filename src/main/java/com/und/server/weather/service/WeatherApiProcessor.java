package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.und.server.weather.client.KmaWeatherClient;
import com.und.server.weather.client.OpenMeteoClient;
import com.und.server.weather.client.OpenMeteoKmaClient;
import com.und.server.weather.config.WeatherProperties;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
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
	private final KmaWeatherClient kmaWeatherClient;
	private final OpenMeteoClient openMeteoClient;
	private final OpenMeteoKmaClient openMeteoKmaClient;
	private final WeatherProperties weatherProperties;
	private final Executor weatherExecutor;

	public WeatherApiProcessor(
		KmaWeatherClient kmaWeatherClient,
		OpenMeteoClient openMeteoClient,
		OpenMeteoKmaClient openMeteoKmaClient,
		WeatherProperties weatherProperties,
		@Qualifier("weatherExecutor") Executor weatherExecutor
	) {
		this.kmaWeatherClient = kmaWeatherClient;
		this.openMeteoClient = openMeteoClient;
		this.openMeteoKmaClient = openMeteoKmaClient;
		this.weatherProperties = weatherProperties;
		this.weatherExecutor = weatherExecutor;
	}


	public WeatherApiResultDto callTodayWeather(
		final WeatherRequest weatherRequest,
		final TimeSlot currentSlot,
		final LocalDate today
	) {
		final Double latitude = weatherRequest.latitude();
		final Double longitude = weatherRequest.longitude();
		final GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);

		CompletableFuture<KmaWeatherResponse> weatherFuture =
			CompletableFuture.supplyAsync(() -> callKmaWeatherApi(gridPoint, currentSlot, today), weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		CompletableFuture<OpenMeteoResponse> openMeteoFuture =
			CompletableFuture.supplyAsync(() -> callOpenMeteoApi(latitude, longitude, today), weatherExecutor)
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
		final LocalDate today, final LocalDate targetDate
	) {
		final Double latitude = weatherRequest.latitude();
		final Double longitude = weatherRequest.longitude();
		final GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);

		CompletableFuture<KmaWeatherResponse> weatherFuture =
			CompletableFuture.supplyAsync(() -> callKmaWeatherApi(gridPoint, timeSlot, today), weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		CompletableFuture<OpenMeteoResponse> openMeteoFuture =
			CompletableFuture.supplyAsync(() -> callOpenMeteoApi(latitude, longitude, targetDate), weatherExecutor)
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
			CompletableFuture.supplyAsync(() -> callOpenMeteoKmaWeatherApi(
					latitude, longitude, targetDate), weatherExecutor)
				.orTimeout(API_TIMEOUT_SEC, TimeUnit.SECONDS);

		CompletableFuture<OpenMeteoResponse> openMeteoFuture =
			CompletableFuture.supplyAsync(() -> callOpenMeteoApi(latitude, longitude, targetDate), weatherExecutor)
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


	private KmaWeatherResponse callKmaWeatherApi(
		final GridPoint gridPoint,
		final TimeSlot timeSlot,
		final LocalDate date
	) {
		final String baseDate = WeatherType.getBaseDate(timeSlot, date).format(WeatherType.KMA_DATE_FORMATTER);
		final String baseTime = WeatherType.getBaseTime(timeSlot);

		try {
			return kmaWeatherClient.getVilageForecast(
				weatherProperties.kma().serviceKey(),
				1,
				1000,
				"JSON",
				baseDate, baseTime,
				gridPoint.gridX(), gridPoint.gridY()
			);
		} catch (ResourceAccessException e) {
			log.error("KMA timeout/network error baseDate={} baseTime={} grid=({},{}), slot={}",
				baseDate, baseTime, gridPoint.gridX(), gridPoint.gridY(), timeSlot, e);
			throw new KmaApiException(WeatherErrorResult.KMA_TIMEOUT, e);

		} catch (HttpClientErrorException e) {
			log.error("KMA 4xx error baseDate={} baseTime={} grid=({},{}), slot={}, status={}",
				baseDate, baseTime, gridPoint.gridX(), gridPoint.gridY(), timeSlot, e.getStatusCode().value(), e);
			throw new KmaApiException(WeatherErrorResult.KMA_BAD_REQUEST, e);

		} catch (HttpServerErrorException e) {
			log.error("KMA 5xx error baseDate={} baseTime={} grid=({},{}), slot={}, status={}",
				baseDate, baseTime, gridPoint.gridX(), gridPoint.gridY(), timeSlot, e.getStatusCode().value(), e);
			throw new KmaApiException(WeatherErrorResult.KMA_SERVER_ERROR, e);

		} catch (RestClientResponseException e) {
			if (e.getStatusCode().value() == 429) {
				log.error("KMA 429(rate limit) baseDate={} baseTime={} grid=({},{}), slot={}",
					baseDate, baseTime, gridPoint.gridX(), gridPoint.gridY(), timeSlot, e);
				throw new KmaApiException(WeatherErrorResult.KMA_RATE_LIMIT, e);
			}
			log.error("KMA response error baseDate={} baseTime={} grid=({},{}), slot={}, status={}",
				baseDate, baseTime, gridPoint.gridX(), gridPoint.gridY(), timeSlot, e.getStatusCode().value(), e);
			throw new KmaApiException(WeatherErrorResult.KMA_API_ERROR, e);

		} catch (Exception e) {
			log.error("KMA call failed(others) baseDate={} baseTime={} grid=({},{}), slot={}",
				baseDate, baseTime, gridPoint.gridX(), gridPoint.gridY(), timeSlot, e);
			throw new KmaApiException(WeatherErrorResult.KMA_API_ERROR, e);
		}
	}

	private OpenMeteoResponse callOpenMeteoApi(
		final Double latitude, final Double longitude,
		final LocalDate date
	) {
		final String variables = String.join(
			",",
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
		} catch (ResourceAccessException e) {
			log.error("Open-Meteo timeout/network error lat={}, lon={}, date={}",
				latitude, longitude, date, e);
			throw new WeatherException(WeatherErrorResult.OPEN_METEO_TIMEOUT, e);

		} catch (HttpClientErrorException e) {
			log.error("Open-Meteo 4xx error lat={}, lon={}, date={}, status={}",
				latitude, longitude, date, e.getStatusCode().value(), e);
			throw new WeatherException(WeatherErrorResult.OPEN_METEO_BAD_REQUEST, e);

		} catch (HttpServerErrorException e) {
			log.error("Open-Meteo 5xx error lat={}, lon={}, date={}, status={}",
				latitude, longitude, date, e.getStatusCode().value(), e);
			throw new WeatherException(WeatherErrorResult.OPEN_METEO_SERVER_ERROR, e);

		} catch (RestClientResponseException e) {
			if (e.getStatusCode().value() == 429) {
				log.error("Open-Meteo 429(rate limit) lat={}, lon={}, date={}",
					latitude, longitude, date, e);
				throw new WeatherException(WeatherErrorResult.OPEN_METEO_RATE_LIMIT, e);
			}
			log.error("Open-Meteo response error lat={}, lon={}, date={}, status={}",
				latitude, longitude, date, e.getStatusCode().value(), e);
			throw new WeatherException(WeatherErrorResult.OPEN_METEO_API_ERROR, e);

		} catch (Exception e) {
			log.error("Open-Meteo call failed(others) lat={}, lon={}, date={}",
				latitude, longitude, date, e);
			throw new WeatherException(WeatherErrorResult.OPEN_METEO_API_ERROR, e);
		}
	}

	private OpenMeteoWeatherResponse callOpenMeteoKmaWeatherApi(
		final Double latitude,
		final Double longitude,
		final LocalDate date
	) {
		try {
			return openMeteoKmaClient.getWeatherForecast(
				latitude,
				longitude,
				WeatherType.OPEN_METEO_VARIABLES,
				date.toString(),
				date.toString(),
				"Asia/Seoul"
			);

		} catch (Exception e) {
			log.error("Open-Meteo KMA call failed lat={}, lon={}, date={}",
				latitude, longitude, date, e);
			throw new WeatherException(WeatherErrorResult.OPEN_METEO_API_ERROR, e);
		}
	}

}
