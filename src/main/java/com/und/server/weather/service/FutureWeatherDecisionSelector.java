package com.und.server.weather.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

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
		return WeatherType.getWorst(weatherTypes);
    }

    public FineDustType calculateWorstFineDust(List<FineDustType> fineDustTypes) {
		if (fineDustTypes == null || fineDustTypes.isEmpty()) {
			return FineDustType.DEFAULT;
		}
		return FineDustType.getWorst(fineDustTypes);
    }

    public UvType calculateWorstUv(List<UvType> uvTypes) {
        if (uvTypes == null || uvTypes.isEmpty()) {
            return UvType.DEFAULT;
        }
		return UvType.getWorst(uvTypes);
    }

}
