package com.und.server.weather.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 미세먼지 등급 enum
 * 한국환경공단 에어코리아 API의 등급코드와 직접 매핑
 */
@Getter
@RequiredArgsConstructor
public enum FineDustLevel {

	GOOD("좋음", 0, 30, 0, 15, 1),
	NORMAL("보통", 31, 80, 16, 35, 2),
	BAD("나쁨", 81, 150, 36, 75, 3),
	VERY_BAD("매우나쁨", 151, Integer.MAX_VALUE, 76, Integer.MAX_VALUE, 4);

	private final String description;
	private final int minPm10; // PM10 농도 최소값 (µg/m³)
	private final int maxPm10; // PM10 농도 최대값 (µg/m³)
	private final int minPm25; // PM2.5 농도 최소값 (µg/m³)
	private final int maxPm25; // PM2.5 농도 최대값 (µg/m³)
	private final int severity; // 심각도 (높을수록 나쁨)

	/**
	 * Open-Meteo API PM10 농도값으로 미세먼지 등급 찾기 (기본 메서드)
	 */
	public static FineDustLevel fromPm10Concentration(double pm10Value) {
		int pm10 = (int) Math.round(pm10Value); // 소수점 반올림

		for (FineDustLevel level : values()) {
			if (pm10 >= level.minPm10 && pm10 <= level.maxPm10) {
				return level;
			}
		}
		return GOOD; // 기본값
	}

	/**
	 * Open-Meteo API PM2.5 농도값으로 미세먼지 등급 찾기
	 */
	public static FineDustLevel fromPm25Concentration(double pm25Value) {
		int pm25 = (int) Math.round(pm25Value); // 소수점 반올림

		for (FineDustLevel level : values()) {
			if (pm25 >= level.minPm25 && pm25 <= level.maxPm25) {
				return level;
			}
		}
		return GOOD; // 기본값
	}

	/**
	 * API 등급 코드로 미세먼지 등급 찾기 (기존 API 호환용)
	 */
	public static FineDustLevel fromKhaiGrade(int khaiGrade) {
		// 1:좋음, 2:보통, 3:나쁨, 4:매우나쁨
		switch (khaiGrade) {
			case 1: return GOOD;
			case 2: return NORMAL;
			case 3: return BAD;
			case 4: return VERY_BAD;
			default: return GOOD;
		}
	}

	/**
	 * 문자열 등급 코드로 미세먼지 등급 찾기 (기존 API 호환용)
	 */
	public static FineDustLevel fromKhaiGrade(String khaiGrade) {
		try {
			int code = Integer.parseInt(khaiGrade);
			return fromKhaiGrade(code);
		} catch (NumberFormatException e) {
			return GOOD; // 파싱 실패시 기본값
		}
	}

	/**
	 * PM10 농도값으로 미세먼지 등급 찾기 (정수형 호환용)
	 */
	public static FineDustLevel fromPm10Value(int pm10Value) {
		return fromPm10Concentration((double) pm10Value);
	}

	/**
	 * PM2.5 농도값으로 미세먼지 등급 찾기 (정수형 호환용)
	 */
	public static FineDustLevel fromPm25Value(int pm25Value) {
		return fromPm25Concentration((double) pm25Value);
	}

	/**
	 * 가장 심각한 미세먼지 등급 반환 (최악 시나리오용)
	 */
	public static FineDustLevel getWorst(FineDustLevel... levels) {
		FineDustLevel worst = GOOD;
		for (FineDustLevel level : levels) {
			if (level.severity > worst.severity) {
				worst = level;
			}
		}
		return worst;
	}
}
