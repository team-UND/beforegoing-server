package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.UvType;
import com.und.server.weather.dto.api.OpenMeteoResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UvIndexExtractor {

	public Map<Integer, UvType> extractUvForHours(
		final OpenMeteoResponse openMeteoResponse,
		final List<Integer> targetHours,
		final LocalDate date
	) {
		Map<Integer, UvType> result = new HashMap<>();

		if (!isValidResponse(openMeteoResponse)) {
			return result;
		}

		List<String> times = openMeteoResponse.hourly().time();
		List<Double> uvIndexValues = openMeteoResponse.hourly().uvIndex();

		for (String str : times) {
			System.out.println(str);
		}
		for (Double d : uvIndexValues) {
			System.out.println(d);
		}

		if (!isValidData(times, uvIndexValues)) {
			return result;
		}

		String targetDateStr = date.toString();

		for (int i = 0; i < times.size(); i++) {
			String timeStr = times.get(i);

			if (!timeStr.startsWith(targetDateStr)) {
				continue;
			}

			try {
				int hour = Integer.parseInt(timeStr.substring(11, 13));
				if (targetHours.contains(hour)) {
					UvType uv = convertToUvType(i, uvIndexValues);

					if (uv != null) {
						result.put(hour, uv);
						System.out.println("자외선" + timeStr);
					}
				}
			} catch (Exception e) {
				log.warn("시간 파싱 실패: {}", timeStr);
			}
		}

		log.debug("배치 UV 추출 완료: {} (총 {}개 시간)", result.size(), targetHours.size());
		return result;
	}


	private UvType convertToUvType(final int index, final List<Double> uvIndexValues) {
		if (index >= uvIndexValues.size()) {
			return null;
		}

		Double uvIndex = uvIndexValues.get(index);

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
