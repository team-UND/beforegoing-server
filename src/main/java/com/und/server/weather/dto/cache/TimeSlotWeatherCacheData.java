package com.und.server.weather.dto.cache;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class TimeSlotWeatherCacheData {

	private Map<String, WeatherCacheData> hours;

	public WeatherCacheData getHourlyData(int hour) {
		String hourKey = getFormatHour(hour);
		return hours.get(hourKey);
	}

	public boolean hasValidDataForHour(int hour) {
		String hourKey = getFormatHour(hour);
		WeatherCacheData weatherCacheData = hours.get(hourKey);

		return weatherCacheData != null && weatherCacheData.isValid();
	}

	@JsonIgnore
	public boolean isValid() {
		return hours != null && !hours.isEmpty();
	}

	private String getFormatHour(int hour) {
		return String.format("%02d", hour);
	}

}
