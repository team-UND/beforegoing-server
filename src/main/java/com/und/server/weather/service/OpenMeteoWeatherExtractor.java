package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OpenMeteoWeatherExtractor {

	public Map<Integer, WeatherType> extractWeatherForHours(
		final OpenMeteoWeatherResponse weatherResponse,
		final List<Integer> targetHours,
		final LocalDate date
	) {
		Map<Integer, WeatherType> result = new HashMap<>();

		if (!isValidResponse(weatherResponse) || targetHours == null || targetHours.isEmpty()) {
			return result;
		}

		List<String> times = weatherResponse.hourly().time();
		List<Integer> weatherCodes = weatherResponse.hourly().weathercode();

		if (!isValidData(times, weatherCodes)) {
			return result;
		}

		final var targetSet = Set.copyOf(targetHours);
		final String targetDateStr = date.toString();

		for (int i = 0; i < times.size(); i++) {
			final String timeStr = times.get(i);
			if (timeStr == null || !timeStr.startsWith(targetDateStr)) {
				continue;
			}

			final int hour;
			try {
				hour = Integer.parseInt(timeStr.substring(11, 13));
			} catch (NumberFormatException e) {
				continue;
			}

			if (!targetSet.contains(hour)) {
				continue;
			}

			final WeatherType weather = convertToWeatherType(i, weatherCodes);
			if (weather != null) {
				result.put(hour, weather);
			}
		}
		return result;
	}

	private WeatherType convertToWeatherType(final int index, final List<Integer> weatherCodes) {
		if (index >= weatherCodes.size()) {
			return null;
		}

		final Integer weatherCode = weatherCodes.get(index);
		if (weatherCode == null) {
			return null;
		}

		return WeatherType.fromOpenMeteoCode(weatherCode);
	}

	private boolean isValidResponse(final OpenMeteoWeatherResponse weatherResponse) {
		return weatherResponse != null && weatherResponse.hourly() != null;
	}

	private boolean isValidData(final List<String> times, final List<Integer> weatherCodes) {
		return times != null && weatherCodes != null;
	}

}
