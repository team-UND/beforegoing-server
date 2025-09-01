package com.und.server.weather.infrastructure.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(

	@JsonProperty("latitude") Double latitude,
	@JsonProperty("longitude") Double longitude,
	@JsonProperty("timezone") String timezone,
	@JsonProperty("hourly_units") HourlyUnits hourlyUnits,
	@JsonProperty("hourly") Hourly hourly

) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record HourlyUnits(
		@JsonProperty("time") String time,
		@JsonProperty("pm2_5") String pm25,
		@JsonProperty("pm10") String pm10,
		@JsonProperty("uv_index") String uvIndex
	) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Hourly(
		@JsonProperty("time") List<String> time,
		@JsonProperty("pm2_5") List<Double> pm25,
		@JsonProperty("pm10") List<Double> pm10,
		@JsonProperty("uv_index") List<Double> uvIndex
	) { }

}
