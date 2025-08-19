package com.und.server.weather.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자외선 지수 등급 enum
 * 기상청 생활기상지수 API의 자외선지수 값과 매핑
 */
@Getter
@RequiredArgsConstructor
public enum UvIndexLevel {

	VERY_LOW("매우낮음", 0, 2, 1),
	LOW("낮음", 3, 4, 2),
	NORMAL("보통", 5, 6, 3),
	HIGH("높음", 7, 9, 4),
	VERY_HIGH("매우높음", 10, Integer.MAX_VALUE, 5);

	private final String description;
	private final int minUvIndex; // 자외선지수 최소값 (h3~h24 값 범위)
	private final int maxUvIndex; // 자외선지수 최대값 (h3~h24 값 범위)
	private final int severity; // 심각도 (높을수록 위험)

	/**
	 * Open-Meteo API 자외선지수 값으로 등급 찾기 (기본 메서드)
	 */
	public static UvIndexLevel fromUvIndex(double uvIndexValue) {
		int uvIndex = (int) Math.round(uvIndexValue); // 소수점 반올림

		for (UvIndexLevel level : values()) {
			if (uvIndex >= level.minUvIndex && uvIndex <= level.maxUvIndex) {
				return level;
			}
		}
		return VERY_LOW; // 기본값
	}

	/**
	 * 정수형 자외선지수 값으로 등급 찾기 (기존 호환용)
	 */
	public static UvIndexLevel fromUvIndex(int uvIndexValue) {
		return fromUvIndex((double) uvIndexValue);
	}

	/**
	 * 가장 높은 자외선 등급 반환 (최악 시나리오용)
	 */
	public static UvIndexLevel getWorst(UvIndexLevel... levels) {
		UvIndexLevel worst = VERY_LOW;
		for (UvIndexLevel level : levels) {
			if (level.severity > worst.severity) {
				worst = level;
			}
		}
		return worst;
	}
}
