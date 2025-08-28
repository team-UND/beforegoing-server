package com.und.server.weather.constants;

import java.time.LocalDate;
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

	public static String getBaseTime(final TimeSlot timeSlot) {
		return switch (timeSlot) {
			case SLOT_00_03 -> "2300";
			case SLOT_03_06 -> "0200";
			case SLOT_06_09 -> "0500";
			case SLOT_09_12 -> "0800";
			case SLOT_12_15 -> "1100";
			case SLOT_15_18 -> "1400";
			case SLOT_18_21 -> "1700";
			case SLOT_21_24 -> "2000";
		};
	}

	public static LocalDate getBaseDate(final TimeSlot timeSlot, final LocalDate date) {
		if (timeSlot == TimeSlot.SLOT_00_03) {
			return date.minusDays(1);
		}
		return date;
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

	public static WeatherType fromOpenMeteoCode(final int weatherCode) {
		return switch (weatherCode) {
			case 0 -> WeatherType.SUNNY;
			case 1, 2, 3 -> WeatherType.CLOUDY;
			case 45, 48 -> WeatherType.OVERCAST;
			case 51, 53, 55, 56, 57 -> WeatherType.RAIN;
			case 61, 63, 65, 66, 67 -> WeatherType.RAIN;
			case 71, 73, 75, 77 -> WeatherType.SNOW;
			case 80, 81, 82 -> WeatherType.SHOWER;
			case 85, 86 -> WeatherType.SLEET;
			default -> WeatherType.DEFAULT;
		};
	}

}
