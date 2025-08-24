package com.und.server.weather.constants;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherType {

	NOTHING(null, null, "없음", 0),
	SUNNY(null, 1, "맑음", 1),
	CLOUDY(null, 3, "구름많음", 2),
	OVERCAST(null, 4, "흐림", 2),
	RAIN(1, null, "비", 5),
	SLEET(2, null, "진눈깨비", 3),
	SNOW(3, null, "눈", 4),
	SHOWER(4, null, "소나기", 6);

	private final Integer ptyValue;
	private final Integer skyValue;
	private final String description;
	private final int severity;

	public static final WeatherType DEFAULT = WeatherType.NOTHING;
	public static final DateTimeFormatter KMA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static WeatherType fromPtyValue(int ptyValue) {
		for (WeatherType type : values()) {
			if (Objects.equals(type.ptyValue, ptyValue)) {
				return type;
			}
		}
		return DEFAULT;
	}

	public static WeatherType fromSkyValue(int skyValue) {
		for (WeatherType type : values()) {
			if (Objects.equals(type.skyValue, skyValue)) {
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
