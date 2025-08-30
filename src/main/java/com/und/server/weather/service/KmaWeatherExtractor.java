package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.infrastructure.dto.KmaWeatherResponse;

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

		if (!isValidInput(weatherResponse, targetHours)) {
			return result;
		}

		final Set<Integer> targetSet = Set.copyOf(targetHours);
		final String targetDateStr = date.format(WeatherType.KMA_DATE_FORMATTER);
		final List<KmaWeatherResponse.WeatherItem> items =
			weatherResponse.response().body().items().item();

		for (KmaWeatherResponse.WeatherItem item : items) {
			processItem(item, targetDateStr, targetSet, result);
		}

		return result;
	}

	private void processItem(
		KmaWeatherResponse.WeatherItem item,
		String targetDateStr,
		Set<Integer> targetSet,
		Map<Integer, WeatherType> result
	) {
		if (!isSupportedCategory(item.category())) {
			return;
		}
		if (!targetDateStr.equals(item.fcstDate())) {
			return;
		}

		Integer hour = parseHour(item.fcstTime());
		if (hour == null || !targetSet.contains(hour)) {
			return;
		}

		WeatherType weather = convertToWeatherType(item.category(), item.fcstValue());
		if (weather == null || weather == WeatherType.DEFAULT) {
			return;
		}

		if (CAT_PTY.equals(item.category())) {
			result.put(hour, weather);
		} else if (!result.containsKey(hour)) {
			result.put(hour, weather);
		}
	}

	private boolean isSupportedCategory(String category) {
		return CAT_PTY.equals(category) || CAT_SKY.equals(category);
	}

	private Integer parseHour(String fcstTime) {
		if (fcstTime == null) {
			return null;
		}
		try {
			return Integer.parseInt(fcstTime) / 100;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private WeatherType convertToWeatherType(final String category, final String fcstValue) {
		if (fcstValue == null) {
			return null;
		}
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

	private boolean isValidInput(KmaWeatherResponse response, List<Integer> targetHours) {
		if (!isValidResponse(response)) {
			return false;
		}
		if (targetHours == null || targetHours.isEmpty()) {
			return false;
		}
		var items = response.response().body().items().item();
		if (items == null || items.isEmpty()) {
			return false;
		}
		return true;
	}

	private boolean isValidResponse(final KmaWeatherResponse weatherResponse) {
		return weatherResponse != null
			&& weatherResponse.response() != null
			&& weatherResponse.response().body() != null
			&& weatherResponse.response().body().items() != null;
	}

}
