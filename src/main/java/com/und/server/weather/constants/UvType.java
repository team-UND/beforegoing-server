package com.und.server.weather.constants;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UvType {

	UNKNOWN("없음", -1, -1, 0),
	VERY_LOW("매우낮음", 0, 2, 1),
	LOW("낮음", 3, 4, 2),
	NORMAL("보통", 5, 6, 3),
	HIGH("높음", 7, 9, 4),
	VERY_HIGH("매우높음", 10, Integer.MAX_VALUE, 5);

	private final String description;
	private final int minUvIndex;
	private final int maxUvIndex;
	private final int severity;

	public static final UvType DEFAULT = UvType.UNKNOWN;

	public static UvType fromUvIndex(double uvIndexValue) {
		int uvIndex = (int) Math.round(uvIndexValue);

		for (UvType level : values()) {
			if (uvIndex >= level.minUvIndex && uvIndex <= level.maxUvIndex) {
				return level;
			}
		}
		return DEFAULT;
	}

	public static UvType getWorst(List<UvType> levels) {
		UvType worst = DEFAULT;
		for (UvType level : levels) {
			if (level.severity > worst.severity) {
				worst = level;
			}
		}
		return worst;
	}

}
