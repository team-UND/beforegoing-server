package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FineDustExtractor {

	public Map<Integer, FineDustType> extractDustForHours(
		final OpenMeteoResponse openMeteoResponse,
		final List<Integer> targetHours,
		final LocalDate date
	) {
		Map<Integer, FineDustType> result = new HashMap<>();

		if (!isValidInput(openMeteoResponse, targetHours)) {
			return result;
		}

		final List<String> times = openMeteoResponse.hourly().time();
		final List<Double> pm10Values = openMeteoResponse.hourly().pm10();
		final List<Double> pm25Values = openMeteoResponse.hourly().pm25();

		if (!isValidData(times, pm10Values, pm25Values)) {
			return result;
		}

		final Set<Integer> targetSet = Set.copyOf(targetHours);
		final String targetDateStr = date.toString();

		for (int i = 0; i < times.size(); i++) {
			processItem(times.get(i), i, targetDateStr, targetSet, pm10Values, pm25Values, result);
		}

		return result;
	}

	private void processItem(
		final String timeStr,
		final int index,
		final String targetDateStr,
		final Set<Integer> targetSet,
		final List<Double> pm10Values,
		final List<Double> pm25Values,
		final Map<Integer, FineDustType> result
	) {
		Integer hour = parseHour(timeStr, targetDateStr);
		if (hour == null || !targetSet.contains(hour)) {
			return;
		}

		FineDustType dust = convertToFineDustType(index, pm10Values, pm25Values);
		if (dust != null) {
			result.put(hour, dust);
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

	private FineDustType convertToFineDustType(
		final int index,
		final List<Double> pm10Values,
		final List<Double> pm25Values
	) {
		if (index >= pm10Values.size() || index >= pm25Values.size()) {
			return null;
		}

		final Double pm10 = pm10Values.get(index);
		final Double pm25 = pm25Values.get(index);
		if (pm10 == null || pm25 == null) {
			return null;
		}

		final FineDustType pm10Level = FineDustType.fromPm10Concentration(pm10);
		final FineDustType pm25Level = FineDustType.fromPm25Concentration(pm25);

		return FineDustType.getWorst(List.of(pm10Level, pm25Level));
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

	private boolean isValidData(
		final List<String> times, final List<Double> pm10Values, final List<Double> pm25Values) {
		return times != null && pm10Values != null && pm25Values != null;
	}

}
