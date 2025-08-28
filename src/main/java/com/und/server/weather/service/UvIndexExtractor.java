package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.UvType;
import com.und.server.weather.dto.api.OpenMeteoResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UvIndexExtractor {

	public Map<Integer, UvType> extractUvForHours(
		final OpenMeteoResponse openMeteoResponse,
		final List<Integer> targetHours,
		final LocalDate date
	) {
		Map<Integer, UvType> result = new HashMap<>();

		if (!isValidResponse(openMeteoResponse) || targetHours == null || targetHours.isEmpty()) {
			return result;
		}

		List<String> times = openMeteoResponse.hourly().time();
		List<Double> uvIndexValues = openMeteoResponse.hourly().uvIndex();

		if (!isValidData(times, uvIndexValues)) {
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

			final UvType uv = convertToUvType(i, uvIndexValues);
			if (uv != null) {
				result.put(hour, uv);
			}
		}
		return result;
	}

	private UvType convertToUvType(final int index, final List<Double> uvIndexValues) {
		if (index >= uvIndexValues.size()) {
			return null;
		}

		final Double uvIndex = uvIndexValues.get(index);
		if (uvIndex == null) {
			return null;
		}

		return UvType.fromUvIndex(uvIndex);
	}

	private boolean isValidResponse(final OpenMeteoResponse openMeteoResponse) {
		return openMeteoResponse != null && openMeteoResponse.hourly() != null;
	}

	private boolean isValidData(final List<String> times, final List<Double> uvIndexValues) {
		return times != null && uvIndexValues != null;
	}

}
