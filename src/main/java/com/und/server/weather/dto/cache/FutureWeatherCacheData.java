package com.und.server.weather.dto.cache;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 미래 날씨 캐시 데이터
 * 계산된 결과만 저장 (시간별 데이터 없음)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FutureWeatherCacheData {
    
    private WeatherType weather;      // 최악의 날씨
    private FineDustType dust;        // 평균 미세먼지
    private UvType uv;               // 평균 UV
    private LocalDateTime lastUpdated; // 마지막 업데이트 시간
    private TimeSlot sourceSlot;      // 데이터 소스 슬롯
    
    /**
     * 데이터 유효성 검사
     */
    public boolean isValid() {
        return weather != null && dust != null && uv != null && lastUpdated != null && sourceSlot != null;
    }
    
    /**
     * WeatherResponse로 변환
     */
    public com.und.server.weather.dto.response.WeatherResponse toWeatherResponse() {
        return com.und.server.weather.dto.response.WeatherResponse.from(weather, dust, uv);
    }
}
