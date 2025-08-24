package com.und.server.weather.dto.cache;

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

	public WeatherCacheData getHourlyData(final int hour) {
		String hourKey = getFormatHour(hour);
		return weatherByHour.get(hourKey);
	}

	public boolean hasValidDataForHour(final int hour) {
		String hourKey = getFormatHour(hour);
		WeatherCacheData weatherCacheData = weatherByHour.get(hourKey);

		return weatherCacheData != null && weatherCacheData.isValid();
	}

	@JsonIgnore
	public boolean isValid() {
		return weatherByHour != null && !weatherByHour.isEmpty();
	}

	private String getFormatHour(final int hour) {
		return String.format("%02d", hour);
	}

	public static TimeSlotWeatherCacheData from(final Map<String, WeatherCacheData> weatherCacheDataByHour) {
		return TimeSlotWeatherCacheData.builder()
			.weatherByHour(weatherCacheDataByHour)
			.build();
	}

}
