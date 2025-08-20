package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.dto.api.OpenMeteoResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FineDustExtractor {

	/**
	 * 특정 시간의 미세먼지 등급 추출
	 */
	public FineDustType extractDustForHour(
		OpenMeteoResponse openMeteoResponse,
		int hour,
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

		// 특정 시간만 추출
		List<Integer> singleHour = List.of(hour);
		List<FineDustType> fineDustTypeList =
			extractFineDustTypes(times, pm10Values, pm25Values, singleHour, date);

		if (fineDustTypeList.isEmpty()) {
			log.debug("미세먼지 데이터 없음 - 날짜: {}, 시간: {}", date, hour);
			return FineDustType.DEFAULT;
		}

		FineDustType dust = fineDustTypeList.get(0);
		log.debug("시간별 미세먼지 등급 추출 완료: {} (시간: {})", dust, hour);
		return dust;
	}

	/**
	 * 여러 시간의 미세먼지를 한 번에 추출 (성능 최적화)
	 */
	public Map<Integer, FineDustType> extractDustForHours(
		OpenMeteoResponse openMeteoResponse,
		List<Integer> targetHours,
		LocalDate date
	) {
		Map<Integer, FineDustType> result = new HashMap<>();
		
		if (!isValidResponse(openMeteoResponse)) {
			log.warn("Open-Meteo 미세먼지 응답 데이터가 비어있음");
			return result;
		}

		List<String> times = openMeteoResponse.getHourly().getTime();
		List<Double> pm10Values = openMeteoResponse.getHourly().getPm10();
		List<Double> pm25Values = openMeteoResponse.getHourly().getPm25();

		if (!isValidData(times, pm10Values, pm25Values)) {
			log.warn("Open-Meteo 미세먼지 데이터가 유효하지 않음");
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
					FineDustType dust = convertToFineDustType(i, pm10Values, pm25Values, timeStr);
					if (dust != null) {
						result.put(hour, dust);
						log.debug("미세먼지 데이터 매핑: {}시 -> {}", hour, dust);
					}
				}
			} catch (Exception e) {
				log.warn("시간 파싱 실패: {}", timeStr);
			}
		}

		log.debug("배치 미세먼지 추출 완료: {} (총 {}개 시간)", result.size(), targetHours.size());
		return result;
	}

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

//		for (String time : times) {
//			System.out.println(time);
//		}
//		for (Double value : pm10Values) {
//			System.out.println(value);
//		}
//		for (Double value : pm25Values) {
//			System.out.println("초미세");
//			System.out.println(value);
//		}

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
				System.out.println(timeStr);
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
