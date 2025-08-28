package com.und.server.weather.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.OpenMeteoWeatherApiResultDto;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.exception.KmaApiException;
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


	public WeatherCacheData getTodayWeatherCache(
		final WeatherRequest weatherRequest, final LocalDateTime nowDateTime
	) {
		Double latitude = weatherRequest.latitude();
		Double longitude = weatherRequest.longitude();
		LocalDate nowDate = nowDateTime.toLocalDate();

		TimeSlot currentSlot = TimeSlot.getCurrentSlot(nowDateTime);
		String cacheKey = keyGenerator.generateTodayKey(latitude, longitude, nowDate, currentSlot);
		String hourKey = keyGenerator.generateTodayHourFieldKey(nowDateTime);

		WeatherCacheData cached = getTodayFromCache(cacheKey, hourKey);
		if (cached != null && cached.isValid()) {
			System.out.println("✅ 캐시 히트! 기존 데이터 사용");
			return cached;
		}

		System.out.println("❌ 캐시 미스! API 호출하여 새로 생성");

		try {
			WeatherApiResultDto weatherApiResult =
				weatherApiProcessor.callTodayWeather(weatherRequest, currentSlot, nowDate);

			Map<String, WeatherCacheData> newData =
				weatherDecisionService.getTodayWeatherCacheData(weatherApiResult, currentSlot, nowDate);

			Duration ttl = ttlCalculator.calculateTtl(currentSlot);
			saveTodayCache(cacheKey, newData, ttl);

			return newData.get(hourKey);

		} catch (KmaApiException e) {
			log.warn("KMA API 실패, Open-Meteo KMA로 fallback", e);
			return handleTodayFallback(weatherRequest, currentSlot, nowDate, cacheKey, hourKey);
		}
	}


	public WeatherCacheData getFutureWeatherCache(
		final WeatherRequest weatherRequest,
		final LocalDateTime nowDateTime, final LocalDate targetDate
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

		try {
			WeatherApiResultDto weatherApiResult =
				weatherApiProcessor.callFutureWeather(
					weatherRequest, currentSlot, nowDateTime.toLocalDate(), targetDate);

			WeatherCacheData futureWeatherCacheData =
				weatherDecisionService.getFutureWeatherCacheData(weatherApiResult, targetDate);

			Duration ttl = ttlCalculator.calculateTtl(currentSlot);
			saveFutureCache(cacheKey, futureWeatherCacheData, ttl);

			return futureWeatherCacheData;

		} catch (KmaApiException e) {
			log.warn("KMA API 실패, Open-Meteo KMA로 fallback", e);
			return handleFutureFallback(weatherRequest, currentSlot, targetDate, cacheKey);
		}
	}


	private WeatherCacheData handleTodayFallback(
		final WeatherRequest weatherRequest,
		final TimeSlot currentSlot,
		final LocalDate nowDate,
		final String cacheKey,
		final String hourKey
	) {
		try {
			OpenMeteoWeatherApiResultDto fallbackResult =
				weatherApiProcessor.callOpenMeteoFallBackWeather(weatherRequest, nowDate);

			Map<String, WeatherCacheData> newData =
				weatherDecisionService.getTodayWeatherCacheDataFallback(fallbackResult, currentSlot, nowDate);

			Duration ttl = ttlCalculator.calculateTtl(currentSlot);
			saveTodayCache(cacheKey, newData, ttl);

			return newData.get(hourKey);

		} catch (Exception e) {
			log.error("Fallback도 실패", e);
			throw e;
		}
	}

	private WeatherCacheData handleFutureFallback(
		final WeatherRequest weatherRequest,
		final TimeSlot currentSlot,
		final LocalDate targetDate,
		final String cacheKey
	) {
		try {
			OpenMeteoWeatherApiResultDto fallbackResult =
				weatherApiProcessor.callOpenMeteoFallBackWeather(weatherRequest, targetDate);

			WeatherCacheData futureWeatherCacheData =
				weatherDecisionService.getFutureWeatherCacheDataFallback(fallbackResult, targetDate);

			Duration ttl = ttlCalculator.calculateTtl(currentSlot);
			saveFutureCache(cacheKey, futureWeatherCacheData, ttl);

			return futureWeatherCacheData;

		} catch (Exception e) {
			log.error("Fallback도 실패", e);
			throw e;
		}
	}

	private WeatherCacheData getTodayFromCache(final String cacheKey, final String hourKey) {
		Object cachedData = redisTemplate.opsForHash().get(cacheKey, hourKey);
		if (cachedData == null) {
			return null;
		}
		return cacheSerializer.deserializeWeatherCacheDataFromHash((String) cachedData);
	}

	private WeatherCacheData getFutureFromCache(final String cacheKey) {
		String cachedJson = redisTemplate.opsForValue().get(cacheKey);
		if (cachedJson == null) {
			return null;
		}
		return cacheSerializer.deserializeWeatherCacheData(cachedJson);
	}

	private void saveTodayCache(
		final String cacheKey,
		final Map<String, WeatherCacheData> data,
		final Duration ttl
	) {
		Map<String, String> hashData = cacheSerializer.serializeWeatherCacheDataToHash(data);
		if (hashData.isEmpty()) {
			return;
		}

		redisTemplate.opsForHash().putAll(cacheKey, hashData);
		redisTemplate.expire(cacheKey, ttl);

		// 디버깅용 출력
		System.out.println("=== REDIS 저장 데이터 (오늘) ===");
		System.out.println("키: " + cacheKey);
		System.out.println("TTL: " + ttl.toMinutes() + "분");
		System.out.println("데이터: " + hashData);
		System.out.println("========================");
	}

	private void saveFutureCache(
		final String cacheKey,
		final WeatherCacheData data,
		final Duration ttl
	) {
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
