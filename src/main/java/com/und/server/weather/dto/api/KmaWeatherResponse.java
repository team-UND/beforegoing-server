package com.und.server.weather.dto.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaWeatherResponse(

	@JsonProperty("response") Response response

) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Response(
		@JsonProperty("header") Header header,
		@JsonProperty("body") Body body
	) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Header(
		@JsonProperty("resultCode") String resultCode,
		@JsonProperty("resultMsg") String resultMsg
	) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Body(
		@JsonProperty("dataType") String dataType,
		@JsonProperty("items") Items items,
		@JsonProperty("totalCount") Integer totalCount
	) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Items(
		@JsonProperty("item") List<WeatherItem> item
	) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record WeatherItem(
		@JsonProperty("baseDate") String baseDate,
		@JsonProperty("baseTime") String baseTime,
		@JsonProperty("category") String category,
		@JsonProperty("fcstDate") String fcstDate,
		@JsonProperty("fcstTime") String fcstTime,
		@JsonProperty("fcstValue") String fcstValue,
		@JsonProperty("nx") Integer nx,
		@JsonProperty("ny") Integer ny
	) { }

}
