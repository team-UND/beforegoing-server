package com.und.server.weather.dto.cache;

import java.util.Map;

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
		String hourKey = String.format("%02d", hour);
		return hours.get(hourKey);
	}

	public boolean hasDataForHour(int hour) {
		String hourKey = String.format("%02d", hour);
		return hours.containsKey(hourKey) && hours.get(hourKey) != null;
	}

	public boolean isValid() {
		return hours != null && !hours.isEmpty();
	}

}
