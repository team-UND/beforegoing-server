package com.und.server.weather.dto.cache;

import java.time.LocalDate;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class WeatherCacheKey {

	private static final String TODAY_PREFIX = "wx:today";
	private static final String FUTURE_PREFIX = "wx:future";
	private static final String DELIMITER = ":";

	private final boolean isToday;
	private final int gridX;
	private final int gridY;
	private final LocalDate date;
	private final TimeSlot slot;

	public static WeatherCacheKey forToday(GridPoint gridPoint, LocalDate today, TimeSlot slot) {
		return WeatherCacheKey.builder()
			.isToday(true)
			.gridX(gridPoint.x())
			.gridY(gridPoint.y())
			.date(today)
			.slot(slot)
			.build();
	}

	public static WeatherCacheKey forFuture(GridPoint gridPoint, LocalDate future, TimeSlot slot) {
		return WeatherCacheKey.builder()
			.isToday(false)
			.gridX(gridPoint.x())
			.gridY(gridPoint.y())
			.date(future)
			.slot(slot)
			.build();
	}

	public String toRedisKey() {
		if (isToday) {
			return String.join(DELIMITER,
				TODAY_PREFIX,
				String.valueOf(gridX),
				String.valueOf(gridY),
				date.toString(),
				slot.name()
			);
		} else {
			return String.join(DELIMITER,
				FUTURE_PREFIX,
				String.valueOf(gridX),
				String.valueOf(gridY),
				date.toString(),
				slot.name()
			);
		}
	}

}
