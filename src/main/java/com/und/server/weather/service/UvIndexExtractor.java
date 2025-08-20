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

	/**
	 * 특정 시간의 UV 지수 추출
	 */
	public UvType extractUvForHour(
		OpenMeteoResponse openMeteoResponse,
		int hour,
		LocalDate date
	) {
		if (!isValidResponse(openMeteoResponse)) {
			log.warn("Open-Meteo 자외선 응답 데이터가 비어있음");
			return UvType.DEFAULT;
		}

		List<String> times = openMeteoResponse.getHourly().getTime();
		List<Double> uvIndexValues = openMeteoResponse.getHourly().getUvIndex();

		if (!isValidData(times, uvIndexValues)) {
			log.warn("Open-Meteo 자외선 데이터가 유효하지 않음");
			return UvType.DEFAULT;
		}

		// 특정 시간만 추출
		List<Integer> singleHour = List.of(hour);
		List<UvType> uvIndexList = extractUvTypes(times, uvIndexValues, singleHour, date);

		if (uvIndexList.isEmpty()) {
			log.debug("자외선 데이터 없음 - 날짜: {}, 시간: {}", date, hour);
			return UvType.DEFAULT;
		}

		UvType uv = uvIndexList.get(0);
		log.debug("시간별 자외선 지수 추출 완료: {} (시간: {})", uv, hour);
		return uv;
	}

	/**
	 * 여러 시간의 UV 지수를 한 번에 추출 (성능 최적화)
	 */
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

	public UvType extractWorstUvIndex(
		OpenMeteoResponse openMeteoResponse,
		List<Integer> targetHours,
		LocalDate date
	) {
		if (!isValidResponse(openMeteoResponse)) {
			log.warn("Open-Meteo 자외선 응답 데이터가 비어있음");
			return UvType.DEFAULT;
		}

		List<String> times = openMeteoResponse.getHourly().getTime();
		List<Double> uvIndexValues = openMeteoResponse.getHourly().getUvIndex();

//		for(String time : times) {
//			System.out.println(time);
//		}
//		for(Double uvIndexValue : uvIndexValues) {
//			System.out.println(uvIndexValue);
//		}
//
//		if (!isValidData(times, uvIndexValues)) {
//			log.warn("Open-Meteo 자외선 데이터가 유효하지 않음");
//			return UvType.DEFAULT;
//		}

		List<UvType> uvIndexList = extractUvTypes(times, uvIndexValues, targetHours, date);

		if (uvIndexList.isEmpty()) {
			log.debug("자외선 데이터 없음 - 날짜: {}, 시간대: {}", date, targetHours);
			return UvType.DEFAULT;
		}

		UvType worst = UvType.getWorst(uvIndexList);
		log.debug("최악 자외선 지수 추출 완료: {} (총 {}개 데이터)", worst, uvIndexList.size());
		return worst;
	}


	private List<UvType> extractUvTypes(
		List<String> times,
		List<Double> uvIndexValues,
		List<Integer> targetHours,
		LocalDate date
	) {
		String targetDateStr = date.toString();
		List<UvType> uvIndexList = new ArrayList<>();

		for (int i = 0; i < times.size(); i++) {
			String timeStr = times.get(i);

			if (!timeStr.startsWith(targetDateStr)) {
				continue;
			}
			if (targetHours != null && !isTargetHour(timeStr, targetHours)) {
				continue;
			}

			UvType level = convertToUvType(i, uvIndexValues, timeStr);
			if (level != null) {
				uvIndexList.add(level);
				System.out.println(timeStr);
				log.debug("자외선 데이터 추가: {} (시간: {})", level, timeStr);
			}
		}
		return uvIndexList;
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

	private boolean isValidData(List<String> times, List<Double> uvIndexValues) {
		return times != null && uvIndexValues != null;
	}

}
