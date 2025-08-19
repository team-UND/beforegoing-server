package com.und.server.weather.dto.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Open-Meteo API 응답 DTO
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
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
	public static class HourlyUnits {

		@JsonProperty("time")
		private String time;

		@JsonProperty("pm2_5")
		private String pm25;          // µg/m³

		@JsonProperty("pm10")
		private String pm10;          // µg/m³

		@JsonProperty("uv_index")
		private String uvIndex;       // UV index
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Hourly {

		@JsonProperty("time")
		private List<String> time;    // ISO 8601 형식 ("2024-01-01T00:00")

		@JsonProperty("pm2_5")
		private List<Double> pm25;    // PM2.5 농도 (µg/m³)

		@JsonProperty("pm10")
		private List<Double> pm10;    // PM10 농도 (µg/m³)

		@JsonProperty("uv_index")
		private List<Double> uvIndex; // 자외선 지수
	}

}
