package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.FineDustLevel;
import com.und.server.weather.dto.api.OpenMeteoResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 미세먼지 데이터 전용 추출 컴포넌트
 */
@Slf4j
@Component
public class FineDustExtractor {

	/**
	 * Open-Meteo 응답에서 최악 미세먼지 추출
	 *
	 * @param response Open-Meteo API 응답
	 * @param targetHours 대상 시간대 (null이면 전체 시간)
	 * @param date 조회 날짜
	 * @return 최악 미세먼지 등급
	 */
	public FineDustLevel extractWorstFineDust(OpenMeteoResponse response, List<Integer> targetHours, LocalDate date) {
		if (!isValidResponse(response)) {
			log.warn("Open-Meteo 미세먼지 응답 데이터가 비어있음");
			return FineDustLevel.GOOD;
		}

		List<String> times = response.getHourly().getTime();
		List<Double> pm10Values = response.getHourly().getPm10();
		List<Double> pm25Values = response.getHourly().getPm25();

		if (!isValidData(times, pm10Values, pm25Values)) {
			log.warn("Open-Meteo 미세먼지 데이터가 유효하지 않음");
			return FineDustLevel.GOOD;
		}

		List<FineDustLevel> fineDustList = extractFineDustLevels(times, pm10Values, pm25Values, targetHours, date);

		if (fineDustList.isEmpty()) {
			log.debug("미세먼지 데이터 없음 - 날짜: {}, 시간대: {}", date, targetHours);
			return FineDustLevel.GOOD; // 기본값
		}

		FineDustLevel worst = FineDustLevel.getWorst(fineDustList.toArray(new FineDustLevel[0]));
		log.debug("최악 미세먼지 등급 추출 완료: {} (총 {}개 데이터)", worst, fineDustList.size());
		return worst;
	}

	/**
	 * 응답 유효성 검사
	 */
	private boolean isValidResponse(OpenMeteoResponse response) {
		return response != null && response.getHourly() != null;
	}

	/**
	 * 데이터 유효성 검사
	 */
	private boolean isValidData(List<String> times, List<Double> pm10Values, List<Double> pm25Values) {
		return times != null && pm10Values != null && pm25Values != null;
	}

	/**
	 * 시간별 미세먼지 등급 추출
	 */
	private List<FineDustLevel> extractFineDustLevels(
		List<String> times,
		List<Double> pm10Values,
		List<Double> pm25Values,
		List<Integer> targetHours,
		LocalDate date
	) {
		String targetDateStr = date.toString(); // yyyy-MM-dd 형식
		List<FineDustLevel> fineDustList = new ArrayList<>();

		for (int i = 0; i < times.size(); i++) {
			String timeStr = times.get(i); // "2024-01-01T12:00" 형식

			// 날짜 필터링
			if (!timeStr.startsWith(targetDateStr)) {
				continue;
			}

			// 시간 필터링
			if (targetHours != null && !isTargetHour(timeStr, targetHours)) {
				continue;
			}

			// PM10, PM2.5 값 추출 및 등급 변환
			FineDustLevel level = convertToFineDustLevel(i, pm10Values, pm25Values, timeStr);
			if (level != null) {
				fineDustList.add(level);
				log.debug("미세먼지 데이터 추가: {} (시간: {})", level, timeStr);
			}
		}

		return fineDustList;
	}

	/**
	 * 시간 필터링 확인
	 */
	private boolean isTargetHour(String timeStr, List<Integer> targetHours) {
		try {
			// "2024-01-01T12:00" -> 12
			int hour = Integer.parseInt(timeStr.substring(11, 13));
			return targetHours.contains(hour);
		} catch (Exception e) {
			log.warn("시간 파싱 실패: {}", timeStr);
			return false;
		}
	}

	/**
	 * PM 농도를 미세먼지 등급으로 변환
	 */
	private FineDustLevel convertToFineDustLevel(
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

		// PM10과 PM2.5 중 더 나쁜 등급 선택
		FineDustLevel pm10Level = FineDustLevel.fromPm10Concentration(pm10);
		FineDustLevel pm25Level = FineDustLevel.fromPm25Concentration(pm25);

		FineDustLevel worseLevel = FineDustLevel.getWorst(pm10Level, pm25Level);
		log.debug("PM 농도 변환: PM10={} ({}), PM2.5={} ({}) -> {}",
			pm10, pm10Level, pm25, pm25Level, worseLevel);

		return worseLevel;
	}
}
