package com.und.server.weather.infrastructure.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoWeatherResponse(

	@JsonProperty("latitude") Double latitude,
	@JsonProperty("longitude") Double longitude,
	@JsonProperty("timezone") String timezone,
	@JsonProperty("hourly_units") HourlyUnits hourlyUnits,
	@JsonProperty("hourly") Hourly hourly

) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record HourlyUnits(
		@JsonProperty("time") String time,
		@JsonProperty("weathercode") String weathercode
	) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Hourly(
		@JsonProperty("time") List<String> time,
		@JsonProperty("weathercode") List<Integer> weathercode
	) { }

}
