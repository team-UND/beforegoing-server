package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.dto.api.OpenMeteoResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FineDustExtractor {

	public FineDustType extractWorstFineDust(
		OpenMeteoResponse openMeteoResponse,
		List<Integer> targetHours,
		LocalDate date
	) {
		if (!isValidResponse(openMeteoResponse)) {
			log.warn("Open-Meteo 미세먼지 응답 데이터가 비어있음");
			return FineDustType.DEFAULT;
		}

		List<String> times = openMeteoResponse.getHourly().getTime();
		List<Double> pm10Values = openMeteoResponse.getHourly().getPm10();
		List<Double> pm25Values = openMeteoResponse.getHourly().getPm25();

		if (!isValidData(times, pm10Values, pm25Values)) {
			log.warn("Open-Meteo 미세먼지 데이터가 유효하지 않음");
			return FineDustType.DEFAULT;
		}

		List<FineDustType> fineDustTypeList =
			extractFineDustTypes(times, pm10Values, pm25Values, targetHours, date);

		if (fineDustTypeList.isEmpty()) {
			log.debug("미세먼지 데이터 없음 - 날짜: {}, 시간대: {}", date, targetHours);
			return FineDustType.DEFAULT;
		}

		FineDustType worst = FineDustType.getWorst(fineDustTypeList);
		log.debug("최악 미세먼지 등급 추출 완료: {} (총 {}개 데이터)", worst, fineDustTypeList.size());
		return worst;
	}


	private List<FineDustType> extractFineDustTypes(
		List<String> times,
		List<Double> pm10Values,
		List<Double> pm25Values,
		List<Integer> targetHours,
		LocalDate date
	) {
		String targetDateStr = date.toString();
		List<FineDustType> fineDustList = new ArrayList<>();

		for (int i = 0; i < times.size(); i++) {
			String timeStr = times.get(i);

			if (!timeStr.startsWith(targetDateStr)) {
				continue;
			}
			if (targetHours != null && !isTargetHour(timeStr, targetHours)) {
				continue;
			}

			FineDustType level = convertToFineDustType(i, pm10Values, pm25Values, timeStr);
			if (level != null) {
				fineDustList.add(level);
				log.debug("미세먼지 데이터 추가: {} (시간: {})", level, timeStr);
			}
		}
		return fineDustList;
	}

	private FineDustType convertToFineDustType(
		int index,
		List<Double> pm10Values,
		List<Double> pm25Values,
		String timeStr
	) {
		if (index >= pm10Values.size() || index >= pm25Values.size()) {
			log.warn("인덱스 범위 초과: {} (PM10 size: {}, PM2.5 size: {})",
				index, pm10Values.size(), pm25Values.size());
			return null;
		}

		Double pm10 = pm10Values.get(index);
		Double pm25 = pm25Values.get(index);

		if (pm10 == null || pm25 == null) {
			log.debug("PM 농도 데이터 null: PM10={}, PM2.5={} (시간: {})", pm10, pm25, timeStr);
			return null;
		}

		FineDustType pm10Level = FineDustType.fromPm10Concentration(pm10);
		FineDustType pm25Level = FineDustType.fromPm25Concentration(pm25);

		FineDustType worseLevel = FineDustType.getWorst(List.of(pm10Level, pm25Level));
		log.debug("PM 농도 변환: PM10={} ({}), PM2.5={} ({}) -> {}",
			pm10, pm10Level, pm25, pm25Level, worseLevel);

		return worseLevel;
	}

	private boolean isTargetHour(String timeStr, List<Integer> targetHours) {
		try {
			int hour = Integer.parseInt(timeStr.substring(11, 13));
			return targetHours.contains(hour);
		} catch (Exception e) {
			log.warn("시간 파싱 실패: {}", timeStr);
			return false;
		}
	}

	private boolean isValidResponse(OpenMeteoResponse openMeteoResponse) {
		return openMeteoResponse != null && openMeteoResponse.getHourly() != null;
	}

	private boolean isValidData(
		List<String> times, List<Double> pm10Values, List<Double> pm25Values) {
		return times != null && pm10Values != null && pm25Values != null;
	}

}
