package com.und.server.weather.dto.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class OpenMeteoResponse {

	@JsonProperty("latitude")
	private Double latitude;

	@JsonProperty("longitude")
	private Double longitude;

	@JsonProperty("timezone")
	private String timezone;

	@JsonProperty("hourly_units")
	private HourlyUnits hourlyUnits;

	@JsonProperty("hourly")
	private Hourly hourly;

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class HourlyUnits {

		@JsonProperty("time")
		private String time;

		@JsonProperty("pm2_5")
		private String pm25;

		@JsonProperty("pm10")
		private String pm10;

		@JsonProperty("uv_index")
		private String uvIndex;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class Hourly {

		@JsonProperty("time")
		private List<String> time;

		@JsonProperty("pm2_5")
		private List<Double> pm25;

		@JsonProperty("pm10")
		private List<Double> pm10;

		@JsonProperty("uv_index")
		private List<Double> uvIndex;
	}

}
