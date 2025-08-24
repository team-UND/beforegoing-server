package com.und.server.weather.constants;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherType {

	UNKNOWN("없음", null, null, 0),
	SUNNY("맑음", null, 1, 1),
	CLOUDY("구름많음", null, 3, 2),
	OVERCAST("흐림", null, 4, 2),
	RAIN("비", 1, null, 5),
	SLEET("진눈깨비", 2, null, 3),
	SNOW("눈", 3, null, 4),
	SHOWER("소나기", 4, null, 6);

	private final String description;
	private final Integer ptyValue;
	private final Integer skyValue;
	private final int severity;

	public static final WeatherType DEFAULT = WeatherType.UNKNOWN;
	public static final DateTimeFormatter KMA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static WeatherType fromPtyValue(final int ptyValue) {
		for (WeatherType type : values()) {
			if (Objects.equals(type.ptyValue, ptyValue)) {
				return type;
			}
		}
		return DEFAULT;
	}

	public static WeatherType fromSkyValue(final int skyValue) {
		for (WeatherType type : values()) {
			if (Objects.equals(type.skyValue, skyValue)) {
				return type;
			}
		}
		return DEFAULT;
	}

	public static WeatherType getWorst(final List<WeatherType> types) {
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
