package com.und.server.weather.dto.cache;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeSlotWeatherCacheData(

	Map<String, WeatherCacheData> weatherByHour

) {

	public WeatherCacheData getHourlyData(int hour) {
		String hourKey = getFormatHour(hour);
		return weatherByHour.get(hourKey);
	}

	public boolean hasValidDataForHour(int hour) {
		String hourKey = getFormatHour(hour);
		WeatherCacheData weatherCacheData = weatherByHour.get(hourKey);

		return weatherCacheData != null && weatherCacheData.isValid();
	}

	@JsonIgnore
	public boolean isValid() {
		return weatherByHour != null && !weatherByHour.isEmpty();
	}

	private String getFormatHour(int hour) {
		return String.format("%02d", hour);
	}

	public static TimeSlotWeatherCacheData from(Map<String, WeatherCacheData> weatherCacheDataByHour) {
		return TimeSlotWeatherCacheData.builder()
			.weatherByHour(weatherCacheDataByHour)
			.build();
	}

	public static TimeSlotWeatherCacheData getEmpty() {
		return new TimeSlotWeatherCacheData(Collections.emptyMap());
	}

}
