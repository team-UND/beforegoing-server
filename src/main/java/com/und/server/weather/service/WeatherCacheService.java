package com.und.server.weather.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.util.CacheSerializer;
import com.und.server.weather.util.WeatherKeyGenerator;
import com.und.server.weather.util.WeatherTtlCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherCacheService {

	private final RedisTemplate<String, String> redisTemplate;
	private final WeatherApiProcessor weatherApiProcessor;
	private final WeatherDecisionService weatherDecisionService;
	private final WeatherKeyGenerator keyGenerator;
	private final WeatherTtlCalculator ttlCalculator;
	private final CacheSerializer cacheSerializer;


	public TimeSlotWeatherCacheData getTodayWeatherCache(WeatherRequest weatherRequest, LocalDateTime nowDateTime) {
		Double latitude = weatherRequest.latitude();
		Double longitude = weatherRequest.longitude();
		LocalDate nowDate = nowDateTime.toLocalDate();

		TimeSlot currentSlot = TimeSlot.getCurrentSlot(nowDateTime);
		String cacheKey = keyGenerator.generateTodayKey(latitude, longitude, nowDate, currentSlot);

		TimeSlotWeatherCacheData cached = getTodayWeatherCache(cacheKey);
		if (cached != null && cached.isValid() && cached.hasValidDataForHour(nowDateTime.getHour())) {
			System.out.println("✅ 캐시 히트! 기존 데이터 사용");
			return cached;
		}

		System.out.println("❌ 캐시 미스! API 호출하여 새로 생성");
		WeatherApiResultDto weatherApiResult =
			weatherApiProcessor.callTodayWeather(weatherRequest, currentSlot, nowDate);

		TimeSlotWeatherCacheData todayWeatherCacheData =
			weatherDecisionService.getTodayWeatherCacheData(weatherApiResult, currentSlot, nowDate);

		Duration ttl = ttlCalculator.calculateTtl(currentSlot);
		saveTodayCache(cacheKey, todayWeatherCacheData, ttl);

		return todayWeatherCacheData;
	}


	public WeatherCacheData getFutureWeatherCache(
		WeatherRequest weatherRequest,
		LocalDateTime nowDateTime, LocalDate targetDate
	) {
		Double latitude = weatherRequest.latitude();
		Double longitude = weatherRequest.longitude();

		TimeSlot currentSlot = TimeSlot.getCurrentSlot(nowDateTime);
		String cacheKey = keyGenerator.generateFutureKey(latitude, longitude, targetDate, currentSlot);

		WeatherCacheData cached = getFutureFromCache(cacheKey);
		if (cached != null && cached.isValid()) {
			System.out.println("✅ 캐시 히트! 기존 데이터 사용");
			return cached;
		}

		System.out.println("❌ 캐시 미스! API 호출하여 새로 생성");
		WeatherApiResultDto weatherApiResult =
			weatherApiProcessor.callFutureWeather(
				weatherRequest, currentSlot, nowDateTime.toLocalDate(), targetDate);

		WeatherCacheData futureWeatherCacheData =
			weatherDecisionService.getFutureWeatherCacheData(weatherApiResult, targetDate);

		Duration ttl = ttlCalculator.calculateTtl(currentSlot);
		saveFutureCache(cacheKey, futureWeatherCacheData, ttl);

		return futureWeatherCacheData;
	}


	private TimeSlotWeatherCacheData getTodayWeatherCache(String cacheKey) {
		String cachedJson = redisTemplate.opsForValue().get(cacheKey);
		if (cachedJson == null) {
			return null;
		}
		return cacheSerializer.deserializeTimeSlotWeatherCacheData(cachedJson);
	}

	private WeatherCacheData getFutureFromCache(String cacheKey) {
		String cachedJson = redisTemplate.opsForValue().get(cacheKey);
		if (cachedJson == null) {
			return null;
		}
		return cacheSerializer.deserializeWeatherCacheData(cachedJson);
	}

	private void saveTodayCache(String cacheKey, TimeSlotWeatherCacheData data, Duration ttl) {
		String json = cacheSerializer.serializeTimeSlotWeatherCacheData(data);
		if (json == null) {
			return;
		}

		redisTemplate.opsForValue().set(cacheKey, json, ttl);

		// 디버깅용 출력
		System.out.println("=== REDIS 저장 데이터 (오늘) ===");
		System.out.println("키: " + cacheKey);
		System.out.println("TTL: " + ttl.toMinutes() + "분");
		System.out.println("데이터: " + json);
		System.out.println("========================");
	}

	private void saveFutureCache(String cacheKey, WeatherCacheData data, Duration ttl) {
		String json = cacheSerializer.serializeWeatherCacheData(data);
		if (json == null) {
			return;
		}

		redisTemplate.opsForValue().set(cacheKey, json, ttl);

		// 디버깅용 출력
		System.out.println("=== REDIS 저장 데이터 (미래) ===");
		System.out.println("키: " + cacheKey);
		System.out.println("TTL: " + ttl.toMinutes() + "분");
		System.out.println("데이터: " + json);
		System.out.println("========================");
	}

}
