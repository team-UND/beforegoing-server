package com.und.server.weather.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FutureWeatherDecisionSelector {

	public WeatherType calculateWorstWeather(final List<WeatherType> weatherTypes) {
		if (weatherTypes == null || weatherTypes.isEmpty()) {
			return WeatherType.DEFAULT;
		}
		return WeatherType.getWorst(weatherTypes);
	}

	public FineDustType calculateWorstFineDust(final List<FineDustType> fineDustTypes) {
		if (fineDustTypes == null || fineDustTypes.isEmpty()) {
			return FineDustType.DEFAULT;
		}
		return FineDustType.getWorst(fineDustTypes);
	}

	public UvType calculateWorstUv(final List<UvType> uvTypes) {
		if (uvTypes == null || uvTypes.isEmpty()) {
			return UvType.DEFAULT;
		}
		return UvType.getWorst(uvTypes);
	}

}
