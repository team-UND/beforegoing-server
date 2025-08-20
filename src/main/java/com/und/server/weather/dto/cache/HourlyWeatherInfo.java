package com.und.server.weather.dto.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 시간별 날씨 정보
 * Redis 캐시에 저장되는 시간별 상세 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyWeatherInfo {
    
    private int hour;
    private WeatherType weather;
    private FineDustType dust;
    private UvType uv;
    
    /**
     * 현재 시간별 정보가 유효한지 확인
     */
    public boolean isValid() {
        return weather != null && dust != null && uv != null && hour >= 0 && hour <= 23;
    }
}
