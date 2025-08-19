package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.UvIndexLevel;
import com.und.server.weather.dto.api.OpenMeteoResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 자외선 지수 데이터 전용 추출 컴포넌트
 */
@Slf4j
@Component
public class UvIndexExtractor {

	/**
	 * Open-Meteo 응답에서 최악 자외선 지수 추출
	 *
	 * @param response Open-Meteo API 응답
	 * @param targetHours 대상 시간대 (null이면 전체 시간)
	 * @param date 조회 날짜
	 * @return 최악 자외선 지수 등급
	 */
	public UvIndexLevel extractWorstUvIndex(OpenMeteoResponse response, List<Integer> targetHours, LocalDate date) {
		if (!isValidResponse(response)) {
			log.warn("Open-Meteo 자외선 응답 데이터가 비어있음");
			return UvIndexLevel.VERY_LOW;
		}

		List<String> times = response.getHourly().getTime();
		List<Double> uvIndexValues = response.getHourly().getUvIndex();

		if (!isValidData(times, uvIndexValues)) {
			log.warn("Open-Meteo 자외선 데이터가 유효하지 않음");
			return UvIndexLevel.VERY_LOW;
		}

		List<UvIndexLevel> uvIndexList = extractUvIndexLevels(times, uvIndexValues, targetHours, date);

		if (uvIndexList.isEmpty()) {
			log.debug("자외선 데이터 없음 - 날짜: {}, 시간대: {}", date, targetHours);
			return UvIndexLevel.VERY_LOW; // 기본값
		}

		UvIndexLevel worst = UvIndexLevel.getWorst(uvIndexList);
		log.debug("최악 자외선 지수 추출 완료: {} (총 {}개 데이터)", worst, uvIndexList.size());
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
	private boolean isValidData(List<String> times, List<Double> uvIndexValues) {
		return times != null && uvIndexValues != null;
	}

	/**
	 * 시간별 자외선 지수 등급 추출
	 */
	private List<UvIndexLevel> extractUvIndexLevels(
		List<String> times,
		List<Double> uvIndexValues,
		List<Integer> targetHours,
		LocalDate date
	) {
		String targetDateStr = date.toString(); // yyyy-MM-dd 형식
		List<UvIndexLevel> uvIndexList = new ArrayList<>();

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

			// UV 지수 값 추출 및 등급 변환
			UvIndexLevel level = convertToUvIndexLevel(i, uvIndexValues, timeStr);
			if (level != null) {
				uvIndexList.add(level);
				log.debug("자외선 데이터 추가: {} (시간: {})", level, timeStr);
			}
		}

		return uvIndexList;
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
	 * UV 지수를 자외선 등급으로 변환
	 */
	private UvIndexLevel convertToUvIndexLevel(int index, List<Double> uvIndexValues, String timeStr) {
		if (index >= uvIndexValues.size()) {
			log.warn("인덱스 범위 초과: {} (UV 지수 size: {})", index, uvIndexValues.size());
			return null;
		}

		Double uvIndex = uvIndexValues.get(index);

		if (uvIndex == null) {
			log.debug("UV 지수 데이터 null (시간: {})", timeStr);
			return null;
		}

		UvIndexLevel level = UvIndexLevel.fromUvIndex(uvIndex);
		log.debug("UV 지수 변환: {} -> {} (시간: {})", uvIndex, level, timeStr);

		return level;
	}
}
