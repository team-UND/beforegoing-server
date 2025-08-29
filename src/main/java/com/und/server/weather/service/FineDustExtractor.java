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

		if (!isValidResponse(openMeteoResponse) || targetHours == null || targetHours.isEmpty()) {
			return result;
		}

		List<String> times = openMeteoResponse.hourly().time();
		List<Double> pm10Values = openMeteoResponse.hourly().pm10();
		List<Double> pm25Values = openMeteoResponse.hourly().pm25();

		if (!isValidData(times, pm10Values, pm25Values)) {
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

			final FineDustType dust = convertToFineDustType(i, pm10Values, pm25Values);
			if (dust != null) {
				result.put(hour, dust);
			}
		}
		return result;
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

	private boolean isValidResponse(final OpenMeteoResponse openMeteoResponse) {
		return openMeteoResponse != null && openMeteoResponse.hourly() != null;
	}

	private boolean isValidData(
		final List<String> times, final List<Double> pm10Values, final List<Double> pm25Values) {
		return times != null && pm10Values != null && pm25Values != null;
	}

}
