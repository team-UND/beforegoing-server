package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.UvType;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;

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

		if (!isValidInput(openMeteoResponse, targetHours)) {
			return result;
		}

		final List<String> times = openMeteoResponse.hourly().time();
		final List<Double> uvIndexValues = openMeteoResponse.hourly().uvIndex();

		if (!isValidData(times, uvIndexValues)) {
			return result;
		}

		final Set<Integer> targetSet = Set.copyOf(targetHours);
		final String targetDateStr = date.toString();

		for (int i = 0; i < times.size(); i++) {
			processItem(times.get(i), i, targetDateStr, targetSet, uvIndexValues, result);
		}

		return result;
	}

	private void processItem(
		final String timeStr,
		final int index,
		final String targetDateStr,
		final Set<Integer> targetSet,
		final List<Double> uvIndexValues,
		final Map<Integer, UvType> result
	) {
		Integer hour = parseHour(timeStr, targetDateStr);
		if (hour == null || !targetSet.contains(hour)) {
			return;
		}

		UvType uv = convertToUvType(index, uvIndexValues);
		if (uv != null) {
			result.put(hour, uv);
		}
	}

	private Integer parseHour(final String timeStr, final String targetDateStr) {
		if (timeStr == null || !timeStr.startsWith(targetDateStr)) {
			return null;
		}
		try {
			return Integer.parseInt(timeStr.substring(11, 13));
		} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
			return null;
		}
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

	private boolean isValidInput(final OpenMeteoResponse response, final List<Integer> targetHours) {
		if (response == null || response.hourly() == null) {
			return false;
		}
		if (targetHours == null || targetHours.isEmpty()) {
			return false;
		}
		return true;
	}

	private boolean isValidData(final List<String> times, final List<Double> uvIndexValues) {
		return times != null && uvIndexValues != null;
	}

}
