package com.und.server.weather.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.und.server.weather.client.OpenMeteoClient;
import com.und.server.weather.client.OpenMeteoKmaClient;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.api.OpenMeteoResponse;
import com.und.server.weather.dto.api.OpenMeteoWeatherResponse;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.exception.WeatherException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenMeteoApiService {

	private final OpenMeteoClient openMeteoClient;
	private final OpenMeteoKmaClient openMeteoKmaClient;

	public OpenMeteoResponse callDustUvApi(
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

	public OpenMeteoWeatherResponse callWeatherApi(
		final Double latitude, final Double longitude,
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
