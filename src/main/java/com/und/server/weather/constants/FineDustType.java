package com.und.server.weather.constants;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FineDustType {

	UNKNOWN("없음", -1, -1, -1, -1, 0),
	GOOD("좋음", 0, 30, 0, 15, 1),
	NORMAL("보통", 31, 80, 16, 35, 2),
	BAD("나쁨", 81, 150, 36, 75, 3),
	VERY_BAD("매우나쁨", 151, Integer.MAX_VALUE, 76, Integer.MAX_VALUE, 4);

	private final String description;
	private final int minPm10;
	private final int maxPm10;
	private final int minPm25;
	private final int maxPm25;
	private final int severity;

	public static final FineDustType DEFAULT = FineDustType.UNKNOWN;
	public static final String OPEN_METEO_VARIABLES = "pm2_5,pm10";

	public static FineDustType fromPm10Concentration(double pm10Value) {
		int pm10 = (int) Math.round(pm10Value);

		for (FineDustType level : values()) {
			if (pm10 >= level.minPm10 && pm10 <= level.maxPm10) {
				return level;
			}
		}
		return DEFAULT;
	}

	public static FineDustType fromPm25Concentration(double pm25Value) {
		int pm25 = (int) Math.round(pm25Value);

		for (FineDustType level : values()) {
			if (pm25 >= level.minPm25 && pm25 <= level.maxPm25) {
				return level;
			}
		}
		return DEFAULT;
	}

	public static FineDustType getWorst(List<FineDustType> levels) {
		FineDustType worst = DEFAULT;
		for (FineDustType level : levels) {
			if (level.severity > worst.severity) {
				worst = level;
			}
		}
		return worst;
	}

	public double getAverageValue() {
		if (this == UNKNOWN) {
			return 0.0;
		}
		if (maxPm25 == Integer.MAX_VALUE) {
			return minPm25 + 50;
		}
		return (double) (minPm25 + maxPm25) / 2;
	}

	public static FineDustType fromAverageValue(double averageValue) {
		return fromPm25Concentration(averageValue);
	}

}
