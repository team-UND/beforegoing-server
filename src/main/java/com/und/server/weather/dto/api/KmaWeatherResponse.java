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
public class KmaWeatherResponse {

	@JsonProperty("response")
	private Response response;

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class Response {

		@JsonProperty("header")
		private Header header;

		@JsonProperty("body")
		private Body body;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class Header {

		@JsonProperty("resultCode")
		private String resultCode;

		@JsonProperty("resultMsg")
		private String resultMsg;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class Body {

		@JsonProperty("dataType")
		private String dataType;

		@JsonProperty("items")
		private Items items;

		@JsonProperty("totalCount")
		private Integer totalCount;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class Items {

		@JsonProperty("item")
		private List<WeatherItem> item;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class WeatherItem {

		@JsonProperty("baseDate")
		private String baseDate;

		@JsonProperty("baseTime")
		private String baseTime;

		@JsonProperty("category")
		private String category;

		@JsonProperty("fcstDate")
		private String fcstDate;

		@JsonProperty("fcstTime")
		private String fcstTime;

		@JsonProperty("fcstValue")
		private String fcstValue;

		@JsonProperty("nx")
		private Integer nx;

		@JsonProperty("ny")
		private Integer ny;
	}

}
