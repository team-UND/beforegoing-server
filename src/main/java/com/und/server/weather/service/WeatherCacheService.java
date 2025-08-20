package com.und.server.weather.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.und.server.common.exception.ServerException;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.util.GridConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 캐시 관리 서비스
 * Redis 캐시 조회/저장 및 데이터 추출 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final WeatherDataProcessor dataProcessor;
    private final WeatherKeyGenerator keyGenerator;
    private final FutureWeatherDecisionSelector calculationService;
    private final WeatherTtlCalculator ttlCalculator;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * 오늘 날씨 조회 (특정 시간 반환)
     */
    public WeatherResponse getTodayWeather(Double latitude, Double longitude, LocalDateTime requestTime) {
        log.info("오늘 날씨 조회 시작: lat={}, lng={}, time={}", latitude, longitude, requestTime);

        TimeSlot currentSlot = TimeSlot.getCurrentSlot(requestTime);
        GridPoint gridPoint = GridConverter.convertToGrid(latitude, longitude);
        String cacheKey = keyGenerator.generateTodayKey(gridPoint, currentSlot);

        // 캐시 키 출력
        System.out.println("=== 오늘 날씨 캐시 키 ===");
        System.out.println("키: " + cacheKey);
        System.out.println("슬롯: " + currentSlot);
        System.out.println("요청 시간: " + requestTime.getHour() + "시");
        System.out.println("========================");

        // 1. Redis 확인
        TimeSlotWeatherCacheData cached = getTodayWeatherCache(cacheKey);
        if (cached != null && cached.hasDataForHour(requestTime.getHour())) {
            log.debug("캐시 히트: {}", cacheKey);
            System.out.println("✅ 캐시 히트! 기존 데이터 사용");
            return extractTodayData(cached, requestTime.getHour());
        }

        // 2. 캐시 미스 → API 호출 → 저장 → 반환
        log.debug("캐시 미스: {}", cacheKey);
        System.out.println("❌ 캐시 미스! API 호출하여 새로 생성");
		TimeSlotWeatherCacheData data = dataProcessor.fetchTodaySlotData(latitude, longitude, currentSlot);
        // 동적 TTL 계산
        Duration ttl = ttlCalculator.calculateTtl(currentSlot);
		saveTodayCache(cacheKey, data, ttl);

        return extractTodayData(data, requestTime.getHour());
    }

        /**
     * 미래 날씨 조회 (하루 전체 최악/평균값 반환)
     */
    public WeatherResponse getFutureWeather(Double latitude, Double longitude, LocalDate targetDate) {
        log.info("미래 날씨 조회 시작: lat={}, lng={}, date={}", latitude, longitude, targetDate);

        TimeSlot currentSlot = TimeSlot.getCurrentSlot(LocalDateTime.now());
        GridPoint gridPoint = GridConverter.convertToGrid(latitude, longitude);
        String cacheKey = keyGenerator.generateFutureKey(gridPoint, targetDate, currentSlot);

        // 캐시 키 출력
        System.out.println("=== 미래 날씨 캐시 키 ===");
        System.out.println("키: " + cacheKey);
        System.out.println("타겟 날짜: " + targetDate);
        System.out.println("현재 슬롯: " + currentSlot);
        System.out.println("========================");

        // 1. Redis 확인 (미래 캐시용)
        WeatherCacheData cached = getFutureFromCache(cacheKey);
        if (cached != null && cached.isValid()) {
            log.debug("캐시 히트: {}", cacheKey);
            System.out.println("✅ 캐시 히트! 기존 데이터 사용");
            return cached.toWeatherResponse();
        }

        // 2. 캐시 미스 → API 호출 → 계산 → 저장 → 반환
        log.debug("캐시 미스: {}", cacheKey);
        System.out.println("❌ 캐시 미스! API 호출하여 새로 생성");

        // API 호출하여 0~23시 전체 데이터 가져오기
        WeatherCacheData futureWeatherCacheData =
			dataProcessor.fetchFutureDayData(latitude, longitude, targetDate, currentSlot);

        // 동적 TTL 계산
        Duration ttl = ttlCalculator.calculateTtl(currentSlot);
        saveFutureCache(cacheKey, futureWeatherCacheData, ttl);

        return futureWeatherCacheData.toWeatherResponse();
    }

    /**
     * 오늘 데이터 추출 (특정 시간)
     */
    private WeatherResponse extractTodayData(TimeSlotWeatherCacheData data, int hour) {
		WeatherCacheData weatherCacheData = data.getHourlyData(hour);

		System.out.println("찾은캐시");
		System.out.println(weatherCacheData);
        if (weatherCacheData == null || !weatherCacheData.isValid()) {
            log.warn("유효하지 않은 시간별 데이터: hour={}", hour);
            return createDefaultResponse();
        }

        log.debug("오늘 데이터 추출 완료: {}시 - 날씨:{}, 미세먼지:{}, UV:{}",
            hour, weatherCacheData.getWeather(), weatherCacheData.getDust(), weatherCacheData.getUv());

        return WeatherResponse.from(
            weatherCacheData.getWeather(),
            weatherCacheData.getDust(),
            weatherCacheData.getUv()
        );
    }


    /**
     * Redis에서 오늘 캐시 데이터 조회
     */
    private TimeSlotWeatherCacheData getTodayWeatherCache(String cacheKey) {
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson == null) {
                log.debug("캐시 데이터 없음: {}", cacheKey);
                return null;
            }

            // Redis 조회 데이터 출력
            System.out.println("=== REDIS 조회 데이터 (오늘) ===");
            System.out.println("키: " + cacheKey);
            System.out.println("데이터: " + cachedJson);
            System.out.println("========================");

			return objectMapper.readValue(cachedJson, TimeSlotWeatherCacheData.class);
        } catch (JsonProcessingException e) {
            log.error("오늘 캐시 데이터 파싱 실패: {}", cacheKey, e);
            return null;
        } catch (Exception e) {
            log.error("오늘 캐시 조회 중 오류 발생: {}", cacheKey, e);
            return null;
        }
    }

    /**
     * Redis에서 미래 캐시 데이터 조회
     */
    private WeatherCacheData getFutureFromCache(String cacheKey) {
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson == null) {
                log.debug("미래 캐시 데이터 없음: {}", cacheKey);
                return null;
            }

            // Redis 조회 데이터 출력
            System.out.println("=== REDIS 조회 데이터 (미래) ===");
            System.out.println("키: " + cacheKey);
            System.out.println("데이터: " + cachedJson);
            System.out.println("========================");

			return objectMapper.readValue(cachedJson, WeatherCacheData.class);

        } catch (JsonProcessingException e) {
            log.error("미래 캐시 데이터 파싱 실패: {}", cacheKey, e);
            return null;
        } catch (Exception e) {
            log.error("미래 캐시 조회 중 오류 발생: {}", cacheKey, e);
            return null;
        }
    }

        /**
     * 오늘 날씨 캐시 데이터 저장
     */
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

    /**
     * 미래 날씨 캐시 데이터 저장
     */
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

    /**
     * 기본 응답 생성 (오류 시 사용)
     */
    private WeatherResponse createDefaultResponse() {
        return WeatherResponse.from(
            WeatherType.DEFAULT,
            FineDustType.DEFAULT,
            UvType.DEFAULT
        );
    }

    /**
     * 캐시 삭제 (관리용)
     */
    public boolean deleteCache(String cacheKey) {
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.info("캐시 삭제: {} (결과: {})", cacheKey, deleted);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("캐시 삭제 실패: {}", cacheKey, e);
            return false;
        }
    }

    /**
     * 특정 위치의 모든 캐시 삭제 (관리용)
     */
    public void clearLocationCache(Double latitude, Double longitude) {
        GridPoint gridPoint = GridConverter.convertToGrid(latitude, longitude);

        // 오늘 날씨 캐시 삭제
        for (TimeSlot slot : TimeSlot.values()) {
            String todayKey = keyGenerator.generateTodayKey(gridPoint, slot);
            deleteCache(todayKey);
        }

        // 미래 날씨 캐시 삭제 (패턴 매칭)
        String pattern = String.format("wx:future:%d:%d:*", gridPoint.x(), gridPoint.y());
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("미래 캐시 삭제: {}개 키", keys.size());
            }
        } catch (Exception e) {
            log.warn("미래 캐시 삭제 중 오류: {}", e.getMessage());
        }

        log.info("위치별 캐시 전체 삭제 완료: lat={}, lng={}", latitude, longitude);
    }
}
