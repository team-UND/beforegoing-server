package com.und.server.weather.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.und.server.weather.client.KmaWeatherClient;
import com.und.server.weather.config.WeatherProperties;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.exception.KmaApiException;
import com.und.server.weather.exception.WeatherErrorResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KmaApiFacade {

	private final KmaWeatherClient kmaWeatherClient;
	private final WeatherProperties weatherProperties;

	public KmaWeatherResponse callWeatherApi(
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

}
