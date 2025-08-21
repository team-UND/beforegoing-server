package com.und.server.weather.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.cache.WeatherCacheKey;
import com.und.server.weather.util.GridConverter;


@Component
public class WeatherKeyGenerator {

	private static final double CACHE_GRID = 10.0;

	public String generateTodayKey(Double latitude, Double longitude, TimeSlot slot) {
		GridPoint gridPoint = convertToGrid(latitude, longitude);
		WeatherCacheKey cacheKey = WeatherCacheKey.forToday(gridPoint, slot);

		return cacheKey.toRedisKey();
	}

	public String generateFutureKey(Double latitude, Double longitude, LocalDate requestDate, TimeSlot slot) {
		GridPoint gridPoint = convertToGrid(latitude, longitude);
		WeatherCacheKey cacheKey = WeatherCacheKey.forFuture(gridPoint, requestDate, slot);

		return cacheKey.toRedisKey();
	}

	private GridPoint convertToGrid(Double latitude, Double longitude) {
		return GridConverter.convertToCacheGrid(latitude, longitude, CACHE_GRID);
	}

	/**
	 * Redis 키에서 WeatherCacheKey 객체 파싱
	 */
	public WeatherCacheKey parseKey(String redisKey) {
		return WeatherCacheKey.fromRedisKey(redisKey);
	}

}
