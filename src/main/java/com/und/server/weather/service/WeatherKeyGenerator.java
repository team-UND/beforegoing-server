package com.und.server.weather.service;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.cache.WeatherCacheKey;
import com.und.server.weather.util.GridConverter;

/**
 * Redis 캐시 키 생성 전용 서비스
 */
@Component
public class WeatherKeyGenerator {

    /**
     * 오늘 날씨 캐시 키 생성
     */
    public String generateTodayKey(GridPoint gridPoint, TimeSlot slot) {
        WeatherCacheKey cacheKey = WeatherCacheKey.forToday(gridPoint, slot);
        return cacheKey.toRedisKey();
    }

    /**
     * 미래 날씨 캐시 키 생성
     */
    public String generateFutureKey(GridPoint gridPoint, LocalDate date, TimeSlot slot) {
        WeatherCacheKey cacheKey = WeatherCacheKey.forFuture(gridPoint, date, slot);
        return cacheKey.toRedisKey();
    }

    /**
     * 위도/경도로 오늘 날씨 캐시 키 생성
     */
    public String generateTodayKey(Double latitude, Double longitude, TimeSlot slot) {
        GridPoint gridPoint = convertToGrid(latitude, longitude);
        return generateTodayKey(gridPoint, slot);
    }

    /**
     * 위도/경도로 미래 날씨 캐시 키 생성
     */
    public String generateFutureKey(Double latitude, Double longitude, LocalDate date, TimeSlot slot) {
        GridPoint gridPoint = convertToGrid(latitude, longitude);
        return generateFutureKey(gridPoint, date, slot);
    }

    /**
     * Redis 키에서 WeatherCacheKey 객체 파싱
     */
    public WeatherCacheKey parseKey(String redisKey) {
        return WeatherCacheKey.fromRedisKey(redisKey);
    }

    /**
     * 위도/경도를 격자 좌표로 변환
     */
    private GridPoint convertToGrid(Double latitude, Double longitude) {
        return GridConverter.convertToGrid(latitude, longitude);
    }
}
