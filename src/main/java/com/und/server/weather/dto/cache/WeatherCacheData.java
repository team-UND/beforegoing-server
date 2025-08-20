package com.und.server.weather.dto.cache;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.weather.constants.TimeSlot;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Redis 캐시 저장용 날씨 데이터
 * 슬롯별 모든 시간대의 날씨 정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherCacheData {
    
    /**
     * 시간별 날씨 정보 맵
     * Key: "00", "01", "02", "03" (시간을 2자리 문자열로)
     * Value: 해당 시간의 날씨 정보
     */
    private Map<String, HourlyWeatherInfo> hours;
    
    /**
     * 마지막 업데이트 시간
     */
    private LocalDateTime lastUpdated;
    
    /**
     * 데이터 소스가 된 시간 슬롯
     */
    private TimeSlot sourceSlot;
    
    /**
     * 특정 시간의 날씨 정보 조회
     */
    public HourlyWeatherInfo getHourlyData(int hour) {
        String hourKey = String.format("%02d", hour);
        return hours.get(hourKey);
    }
    
    /**
     * 모든 시간대의 날씨 정보 조회 (JSON 직렬화 제외)
     */
    @JsonIgnore
    public Collection<HourlyWeatherInfo> getAllHourlyData() {
        return hours.values();
    }
    
    /**
     * 캐시 데이터가 유효한지 확인
     */
    public boolean isValid() {
        return hours != null && !hours.isEmpty() && lastUpdated != null && sourceSlot != null;
    }
    
    /**
     * 특정 시간의 데이터가 존재하는지 확인
     */
    public boolean hasDataForHour(int hour) {
        String hourKey = String.format("%02d", hour);
        return hours.containsKey(hourKey) && hours.get(hourKey) != null;
    }
}
