package com.und.server.weather.dto.cache;

import java.time.LocalDate;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;

import lombok.Builder;

@Builder
public record WeatherCacheKey(

	boolean isToday,
	int gridX,
	int gridY,
	LocalDate date,
	TimeSlot slot

) {

	private static final String PREFIX = "wx";
	private static final String TODAY_PREFIX = "today";
	private static final String FUTURE_PREFIX = "future";
	private static final String DELIMITER = ":";

	public static WeatherCacheKey forToday(
		final GridPoint gridPoint, final LocalDate today, final TimeSlot timeSlot
	) {
		return WeatherCacheKey.builder()
			.isToday(true)
			.gridX(gridPoint.gridX())
			.gridY(gridPoint.gridY())
			.date(today)
			.slot(timeSlot)
			.build();
	}

	public static WeatherCacheKey forFuture(
		final GridPoint gridPoint, final LocalDate future, final TimeSlot timeSlot
	) {
		return WeatherCacheKey.builder()
			.isToday(false)
			.gridX(gridPoint.gridX())
			.gridY(gridPoint.gridY())
			.date(future)
			.slot(timeSlot)
			.build();
	}

	public String toRedisKey() {
		if (isToday) {
			return String.join(DELIMITER,
				PREFIX,
				TODAY_PREFIX,
				String.valueOf(gridX),
				String.valueOf(gridY),
				date.toString(),
				slot.name()
			);
		} else {
			return String.join(DELIMITER,
				PREFIX,
				FUTURE_PREFIX,
				String.valueOf(gridX),
				String.valueOf(gridY),
				date.toString(),
				slot.name()
			);
		}
	}

}
