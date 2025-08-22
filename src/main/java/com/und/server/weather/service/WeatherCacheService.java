package com.und.server.weather.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.und.server.common.exception.ServerException;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.exception.WeatherErrorResult;
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
	private final WeatherKeyGenerator keyGenerator;
	private final WeatherTtlCalculator ttlCalculator;
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


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
		TimeSlotWeatherCacheData todayWeatherCacheData =
			weatherApiProcessor.fetchTodaySlotData(
				weatherRequest, currentSlot, nowDate);

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
		WeatherCacheData futureWeatherCacheData =
			weatherApiProcessor.fetchFutureDayData(
				weatherRequest, currentSlot, nowDateTime.toLocalDate(), targetDate);

		// 동적 TTL 계산
		Duration ttl = ttlCalculator.calculateTtl(currentSlot);
		saveFutureCache(cacheKey, futureWeatherCacheData, ttl);

		return futureWeatherCacheData;
	}


	private TimeSlotWeatherCacheData getTodayWeatherCache(String cacheKey) {
		try {
			String cachedJson = redisTemplate.opsForValue().get(cacheKey);
			if (cachedJson == null) {
				return null;
			}
			return objectMapper.readValue(cachedJson, TimeSlotWeatherCacheData.class);

		} catch (JsonProcessingException e) {
			log.error("오늘 캐시 데이터 파싱 실패: {}", cacheKey, e);
			return null;
		} catch (Exception e) {
			log.error("오늘 캐시 조회 중 오류 발생: {}", cacheKey, e);
			return null;
		}
	}

	private WeatherCacheData getFutureFromCache(String cacheKey) {
		try {
			String cachedJson = redisTemplate.opsForValue().get(cacheKey);
			if (cachedJson == null) {
				return null;
			}
			return objectMapper.readValue(cachedJson, WeatherCacheData.class);

		} catch (JsonProcessingException e) {
			log.error("미래 캐시 데이터 파싱 실패: {}", cacheKey, e);
			return null;
		} catch (Exception e) {
			log.error("미래 캐시 조회 중 오류 발생: {}", cacheKey, e);
			return null;
		}
	}


	private void saveTodayCache(String cacheKey, TimeSlotWeatherCacheData data, Duration ttl) {
		try {
			String json = objectMapper.writeValueAsString(data);
			redisTemplate.opsForValue().set(cacheKey, json, ttl);

			// Redis 저장 데이터 출력
			System.out.println("=== REDIS 저장 데이터 (오늘) ===");
			System.out.println("키: " + cacheKey);
			System.out.println("TTL: " + ttl.toMinutes() + "분");
			System.out.println("데이터: " + json);
			System.out.println("========================");

			log.debug("오늘 캐시 데이터 저장 성공: {} (TTL: {})", cacheKey, ttl);

		} catch (JsonProcessingException e) {
			log.error("오늘 캐시 데이터 직렬화 실패: {}", cacheKey, e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		} catch (Exception e) {
			log.error("오늘 캐시 저장 중 오류 발생: {}", cacheKey, e);
			// 캐시 저장 실패는 서비스를 중단하지 않음 (경고만 로그)
		}
	}

	private void saveFutureCache(String cacheKey, WeatherCacheData data, Duration ttl) {
		try {
			String json = objectMapper.writeValueAsString(data);
			redisTemplate.opsForValue().set(cacheKey, json, ttl);

			// Redis 저장 데이터 출력
			System.out.println("=== REDIS 저장 데이터 (미래) ===");
			System.out.println("키: " + cacheKey);
			System.out.println("TTL: " + ttl.toMinutes() + "분");
			System.out.println("데이터: " + json);
			System.out.println("========================");

			log.debug("미래 캐시 데이터 저장 성공: {} (TTL: {})", cacheKey, ttl);

		} catch (JsonProcessingException e) {
			log.error("미래 캐시 데이터 직렬화 실패: {}", cacheKey, e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		} catch (Exception e) {
			log.error("미래 캐시 저장 중 오류 발생: {}", cacheKey, e);
			// 캐시 저장 실패는 서비스를 중단하지 않음 (경고만 로그)
		}
	}

}
