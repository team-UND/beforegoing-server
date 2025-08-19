package com.und.server.weather.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 기상 상태 enum
 * 기상청 단기예보 API의 강수형태(PTY) 코드와 매핑
 * 구름/맑음은 제외하고 강수 형태만 처리
 */
@Getter
@RequiredArgsConstructor
public enum WeatherType {

	// PTY (강수형태) 값들 - 우선순위: 비=소나기 > 눈 > 진눈깨비
	NOTHING(0, "없음", 0),
	RAIN(1, "비", 4),
	SLEET(2, "진눈깨비", 1), // 비+눈
	SNOW(3, "눈", 2),
	SHOWER(4, "소나기", 3); // 비와 동일한 우선순위

	private final int fcstValue; // PTY의 fcstValue
	private final String description;
	private final int severity; // 심각도 (높을수록 나쁨)

	/**
	 * PTY 값으로 강수 상태 찾기 (SKY는 무시 - 강수만 관심)
	 * PTY가 0이면 null 반환 (강수 없음)
	 */
	public static WeatherType fromPtyValue(int ptyValue) {
		for (WeatherType type : values()) {
			if (type.fcstValue == ptyValue) {
				return type;
			}
		}
		return NOTHING; // 알 수 없는 PTY 값
	}

	/**
	 * 가장 심각한 날씨 반환 (최악 시나리오용)
	 * null 값은 무시하고 처리
	 */
	public static WeatherType getWorst(WeatherType... types) {
		WeatherType worst = null;

		for (WeatherType type : types) {
			if (type != null) {
				if (worst == null || type.severity > worst.severity) {
					worst = type;
				}
			}
		}

		return worst; // 모든 값이 null이면 null 반환 (강수 없음)
	}
}
