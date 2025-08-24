package com.und.server.weather.util;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.cache.WeatherCacheKey;


@Component
public class WeatherKeyGenerator {

	private static final double CACHE_GRID = 10.0;

	public String generateTodayKey(
		final Double latitude, final Double longitude,
		final LocalDate today,
		final TimeSlot slot
	) {
		GridPoint gridPoint = convertToGrid(latitude, longitude);
		WeatherCacheKey cacheKey = WeatherCacheKey.forToday(gridPoint, today, slot);

		return cacheKey.toRedisKey();
	}

	public String generateFutureKey(
		final Double latitude, final Double longitude,
		final LocalDate requestDate,
		final TimeSlot slot
	) {
		GridPoint gridPoint = convertToGrid(latitude, longitude);
		WeatherCacheKey cacheKey = WeatherCacheKey.forFuture(gridPoint, requestDate, slot);

		return cacheKey.toRedisKey();
	}

	private GridPoint convertToGrid(final Double latitude, final Double longitude) {
		return GridConverter.convertToCacheGrid(latitude, longitude, CACHE_GRID);
	}

}
