package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.api.KmaWeatherResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KmaWeatherExtractor {

	private static final String CAT_PTY = "PTY";
	private static final String CAT_SKY = "SKY";

	public Map<Integer, WeatherType> extractWeatherForHours(
		final KmaWeatherResponse weatherResponse,
		final List<Integer> targetHours,
		final LocalDate date
	) {
		Map<Integer, WeatherType> result = new HashMap<>();

		if (!isValidResponse(weatherResponse) || targetHours == null || targetHours.isEmpty()) {
			return result;
		}

		final var targetSet = Set.copyOf(targetHours);
		final String targetDateStr = date.format(WeatherType.KMA_DATE_FORMATTER);

		var items = weatherResponse.response().body().items().item();
		if (items == null || items.isEmpty()) {
			return result;
		}

		for (KmaWeatherResponse.WeatherItem item : items) {
			final String category = item.category();
			if (!CAT_PTY.equals(category) && !CAT_SKY.equals(category)) {
				continue;
			}
			if (!targetDateStr.equals(item.fcstDate())) {
				continue;
			}

			final String fcstTime = item.fcstTime();
			final String fcstValue = item.fcstValue();
			if (fcstTime == null || fcstValue == null) {
				continue;
			}

			final int hour;
			try {
				hour = Integer.parseInt(fcstTime) / 100;
			} catch (NumberFormatException e) {
				continue;
			}
			if (!targetSet.contains(hour)) {
				continue;
			}

			final WeatherType weather = convertToWeatherType(category, fcstValue);
			if (weather == null) {
				continue;
			}

			if (CAT_PTY.equals(category)) {
				if (weather != WeatherType.DEFAULT) {
					result.put(hour, weather);
				}
				continue;
			}
			if (!result.containsKey(hour)) {
				result.put(hour, weather);
			}
		}
		return result;
	}

	private WeatherType convertToWeatherType(final String category, final String fcstValue) {
		try {
			int value = Integer.parseInt(fcstValue);
			return switch (category) {

				case CAT_PTY -> WeatherType.fromPtyValue(value);
				case CAT_SKY -> WeatherType.fromSkyValue(value);

				default -> WeatherType.DEFAULT;
			};
		} catch (NumberFormatException e) {
			return WeatherType.DEFAULT;
		}
	}

	private boolean isValidResponse(final KmaWeatherResponse weatherResponse) {
		return weatherResponse != null
			&& weatherResponse.response() != null
			&& weatherResponse.response().body() != null
			&& weatherResponse.response().body().items() != null;
	}

}
