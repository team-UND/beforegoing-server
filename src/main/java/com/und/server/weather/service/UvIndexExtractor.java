package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.ArrayList;
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
		OpenMeteoResponse openMeteoResponse,
		List<Integer> targetHours,
		LocalDate date
	) {
		Map<Integer, UvType> result = new HashMap<>();

		if (!isValidResponse(openMeteoResponse)) {
			log.warn("Open-Meteo 자외선 응답 데이터가 비어있음");
			return result;
		}

		List<String> times = openMeteoResponse.getHourly().getTime();
		List<Double> uvIndexValues = openMeteoResponse.getHourly().getUvIndex();

		for(String str : times) {
			System.out.println(str);
		}
		for(Double d : uvIndexValues) {
			System.out.println(d);
		}

		if (!isValidData(times, uvIndexValues)) {
			log.warn("Open-Meteo 자외선 데이터가 유효하지 않음");
			return result;
		}

		String targetDateStr = date.toString();

		// 한 번만 파싱해서 시간별로 매핑
		for (int i = 0; i < times.size(); i++) {
			String timeStr = times.get(i);

			if (!timeStr.startsWith(targetDateStr)) {
				continue;
			}

			try {
				int hour = Integer.parseInt(timeStr.substring(11, 13));
				if (targetHours.contains(hour)) {
					UvType uv = convertToUvType(i, uvIndexValues, timeStr);
					if (uv != null) {
						result.put(hour, uv);
						System.out.println("자외선" + timeStr);
						log.debug("UV 데이터 매핑: {}시 -> {}", hour, uv);
					}
				}
			} catch (Exception e) {
				log.warn("시간 파싱 실패: {}", timeStr);
			}
		}

		log.debug("배치 UV 추출 완료: {} (총 {}개 시간)", result.size(), targetHours.size());
		return result;
	}


	private UvType convertToUvType(int index, List<Double> uvIndexValues, String timeStr) {
		if (index >= uvIndexValues.size()) {
			log.warn("인덱스 범위 초과: {} (UV 지수 size: {})", index, uvIndexValues.size());
			return null;
		}

		Double uvIndex = uvIndexValues.get(index);

		if (uvIndex == null) {
			log.debug("UV 지수 데이터 null (시간: {})", timeStr);
			return null;
		}

		UvType level = UvType.fromUvIndex(uvIndex);
		log.debug("UV 지수 변환: {} -> {} (시간: {})", uvIndex, level, timeStr);

		return level;
	}

	private boolean isValidResponse(OpenMeteoResponse openMeteoResponse) {
		return openMeteoResponse != null && openMeteoResponse.getHourly() != null;
	}

	private boolean isValidData(List<String> times, List<Double> uvIndexValues) {
		return times != null && uvIndexValues != null;
	}

}
