package com.und.server.weather.dto.cache;

import java.time.LocalDate;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;

import lombok.Builder;
import lombok.Getter;

/**
 * Redis 캐시 키 생성 및 파싱 유틸리티
 */
@Getter
@Builder
public class WeatherCacheKey {
    
    private static final String TODAY_PREFIX = "wx:today";
    private static final String FUTURE_PREFIX = "wx:future";
    private static final String DELIMITER = ":";
    
    private final boolean isToday;
    private final int gridX;
    private final int gridY;
    private final LocalDate date;  // 날짜 추가
    private final TimeSlot slot;
    
        /**
     * 오늘 날씨 캐시 키 생성
     */
    public static WeatherCacheKey forToday(GridPoint gridPoint, TimeSlot slot) {
        return WeatherCacheKey.builder()
            .isToday(true)
            .gridX(gridPoint.x())
            .gridY(gridPoint.y())
            .date(LocalDate.now())
            .slot(slot)
            .build();
    }

    /**
     * 미래 날씨 캐시 키 생성
     */
    public static WeatherCacheKey forFuture(GridPoint gridPoint, LocalDate date, TimeSlot slot) {
        return WeatherCacheKey.builder()
            .isToday(false)
            .gridX(gridPoint.x())
            .gridY(gridPoint.y())
            .date(date)
            .slot(slot)
            .build();
    }
    
    /**
     * Redis 키 문자열 생성
     * 형식: wx:today:{gridX}:{gridY}:{slot} 또는 wx:future:{gridX}:{gridY}:{date}:{slot}
     */
    public String toRedisKey() {
        String prefix = isToday ? TODAY_PREFIX : FUTURE_PREFIX;
        if (isToday) {
            return String.join(DELIMITER, 
                prefix, 
                String.valueOf(gridX), 
                String.valueOf(gridY), 
                slot.name()
            );
        } else {
            return String.join(DELIMITER, 
                prefix, 
                String.valueOf(gridX), 
                String.valueOf(gridY), 
                date.toString(),
                slot.name()
            );
        }
    }
    
    /**
     * Redis 키 문자열에서 WeatherCacheKey 객체 파싱
     */
    public static WeatherCacheKey fromRedisKey(String redisKey) {
        String[] parts = redisKey.split(DELIMITER);
        
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid redis key format: " + redisKey);
        }
        
        boolean isToday = TODAY_PREFIX.equals(parts[0] + DELIMITER + parts[1]);
        
        if (isToday) {
            // 오늘: wx:today:{gridX}:{gridY}:{slot}
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid today redis key format: " + redisKey);
            }
            
            int gridX = Integer.parseInt(parts[2]);
            int gridY = Integer.parseInt(parts[3]);
            TimeSlot slot = TimeSlot.valueOf(parts[4]);
            
            return WeatherCacheKey.builder()
                .isToday(true)
                .gridX(gridX)
                .gridY(gridY)
                .date(LocalDate.now())
                .slot(slot)
                .build();
        } else {
            // 미래: wx:future:{gridX}:{gridY}:{date}:{slot}
            if (parts.length != 6) {
                throw new IllegalArgumentException("Invalid future redis key format: " + redisKey);
            }
            
            int gridX = Integer.parseInt(parts[2]);
            int gridY = Integer.parseInt(parts[3]);
            LocalDate date = LocalDate.parse(parts[4]);
            TimeSlot slot = TimeSlot.valueOf(parts[5]);
            
            return WeatherCacheKey.builder()
                .isToday(false)
                .gridX(gridX)
                .gridY(gridY)
                .date(date)
                .slot(slot)
                .build();
        }
    }
    
    @Override
    public String toString() {
        return toRedisKey();
    }
}
