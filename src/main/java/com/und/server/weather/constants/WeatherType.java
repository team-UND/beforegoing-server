package com.und.server.weather.constants;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherType {

	NOTHING(0, "없음", 0),
	RAIN(1, "비", 4),
	SLEET(2, "진눈깨비", 1),
	SNOW(3, "눈", 2),
	SHOWER(4, "소나기", 3);

	private final int fcstValue;
	private final String description;
	private final int severity;

	public static final WeatherType DEFAULT = WeatherType.NOTHING;

	public static WeatherType fromPtyValue(int ptyValue) {
		for (WeatherType type : values()) {
			if (type.fcstValue == ptyValue) {
				return type;
			}
		}
		return DEFAULT;
	}

	public static WeatherType getWorst(List<WeatherType> types) {
		WeatherType worst = DEFAULT;
		for (WeatherType type : types) {
			if (type != null) {
				if (type.severity > worst.severity) {
					worst = type;
				}
			}
		}
		return worst;
	}

}
