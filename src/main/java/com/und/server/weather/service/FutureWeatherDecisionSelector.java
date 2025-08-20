package com.und.server.weather.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;

import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 데이터 계산 서비스
 * 최악값, 평균값 등의 계산 로직을 담당
 */
@Slf4j
@Component
public class FutureWeatherDecisionSelector {

    public WeatherType calculateWorstWeather(List<WeatherType> weatherTypes) {
        if (weatherTypes == null || weatherTypes.isEmpty()) {
            return WeatherType.DEFAULT;
        }
		WeatherType worst = WeatherType.DEFAULT;
		for (WeatherType type : weatherTypes) {
			if (type != null) {
				if (type.getSeverity() > worst.getSeverity()) {
					worst = type;
				}
			}
		}
		return worst;
    }

    public FineDustType calculateAverageDust(List<FineDustType> fineDustTypes) {
		if (fineDustTypes == null || fineDustTypes.isEmpty()) {
			return FineDustType.DEFAULT;
		}

		double averageValue = fineDustTypes.stream()
			.filter(dust -> dust != null && dust != FineDustType.UNKNOWN)
			.mapToDouble(FineDustType::getAverageValue)
			.average()
			.orElse(0.0);

		return FineDustType.fromAverageValue(averageValue);
    }

    public UvType calculateAverageUv(List<UvType> uvTypes) {
        if (uvTypes == null || uvTypes.isEmpty()) {
            return UvType.DEFAULT;
        }

		double averageValue = uvTypes.stream()
			.filter(uv -> uv != null && uv != UvType.UNKNOWN)
			.mapToDouble(UvType::getAverageValue)
			.average()
			.orElse(0.0);

		return UvType.fromAverageValue(averageValue);
    }

}
